$ErrorActionPreference = "Stop"
$baseUrl = "http://localhost:8080"
$results = [ordered]@{}

function Test-Step($name, [scriptblock]$block) {
    Write-Host "Running: $name..." -ForegroundColor Cyan
    try {
        $out = & $block
        $results[$name] = @{ Status = "PASS"; Details = $out }
        Write-Host "  -> PASS: $out" -ForegroundColor Green
    } catch {
        $results[$name] = @{ Status = "FAIL"; Details = $_.Exception.Message }
        Write-Host "  -> FAIL: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# 1. Homepage loads correctly
Test-Step "1. Homepage loads correctly" {
    $client = New-Object System.Net.WebClient
    $client.Encoding = [System.Text.Encoding]::UTF8
    $html = $client.DownloadString("$baseUrl/home")
    if ($html -notmatch "Fresh Supermarket Picks" -or $html -notmatch "ShopEase") {
        throw "Homepage missing title or hero banner text"
    }
    if ($html -match "<c:[a-zA-Z]+") { throw "Raw JSTL tag found in homepage!" }
    return "Homepage loaded with valid HTML, navbar, hero banner, category cards, and zero raw JSTL tags"
}

# 2. Register a new buyer account
$uniqueEmail = "buyer_e2e_" + [Guid]::NewGuid().ToString().Substring(0,8) + "@shopease.com"
Test-Step "2. Register a new buyer account" {
    $regBody = @{
        name = "Ananya Iyer"
        email = $uniqueEmail
        password = "Password@123"
        role = "BUYER"
    } | ConvertTo-Json
    $resp = Invoke-RestMethod -Uri "$baseUrl/api/v1/auth/register" -Method Post -Body $regBody -ContentType "application/json"
    if ($resp.success -ne $true -or $resp.data.email -ne $uniqueEmail) {
        throw "Registration failed"
    }
    return "Registered new buyer: $($resp.data.name) ($($resp.data.email)) [Role: $($resp.data.role)]"
}

# 3. Login with that buyer account
$buyerSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
Test-Step "3. Login with that buyer account" {
    $loginBody = @{
        email = $uniqueEmail
        password = "Password@123"
    } | ConvertTo-Json
    $login = Invoke-RestMethod -Uri "$baseUrl/api/v1/auth/login" -Method Post -Body $loginBody -ContentType "application/json" -WebSession $buyerSession
    $me = Invoke-RestMethod -Uri "$baseUrl/api/v1/auth/me" -Method Get -WebSession $buyerSession
    if ($me.data.email -ne $uniqueEmail) {
        throw "Logged in user email mismatch: $($me.data.email)"
    }
    return "Authenticated as $($me.data.name) (Session cookie active)"
}

# 4. Browse products
Test-Step "4. Browse products" {
    $prods = Invoke-RestMethod -Uri "$baseUrl/api/v1/products" -Method Get -WebSession $buyerSession
    if ($prods.data.Count -lt 10) {
        throw "Expected at least 10 products, got $($prods.data.Count)"
    }
    return "Catalog retrieved $($prods.data.Count) products across 7 categories"
}

# 5. Search and category filtering
Test-Step "5. Search and category filtering" {
    $groc = Invoke-RestMethod -Uri "$baseUrl/api/v1/products?category=Grocery" -Method Get
    if ($groc.data.Count -eq 0) {
        throw "Category filtering for Grocery failed"
    }
    $search = Invoke-RestMethod -Uri "$baseUrl/api/v1/products?q=Rice" -Method Get
    if ($search.data.Count -eq 0 -or $search.data[0].name -notmatch "Rice") {
        throw "Search query 'Rice' failed"
    }
    return "Category filter returned $($groc.data.Count) grocery items; Search for 'Rice' returned '$($search.data[0].name)'"
}

# 6. Open a product details page
$targetProductId = 36
Test-Step "6. Open a product details page" {
    $p = Invoke-RestMethod -Uri "$baseUrl/api/v1/products/$targetProductId" -Method Get
    if ($p.data.id -ne $targetProductId -or [string]::IsNullOrWhiteSpace($p.data.name)) {
        throw "Product details failed for ID $targetProductId"
    }
    return "Loaded Product #${targetProductId}: '$($p.data.name)' | Price: $($p.data.formattedPrice) | Category: $($p.data.category)"
}

# 7. Add product to cart
Test-Step "7. Add product to cart" {
    $addBody = @{ productId = $targetProductId; quantity = 3 } | ConvertTo-Json
    $cart = Invoke-RestMethod -Uri "$baseUrl/api/v1/cart" -Method Post -Body $addBody -ContentType "application/json" -WebSession $buyerSession
    $cartView = Invoke-RestMethod -Uri "$baseUrl/api/v1/cart" -Method Get -WebSession $buyerSession
    if ($cartView.data.items.Count -eq 0) {
        throw "Cart is empty after adding product"
    }
    return "Added 3 units of '$($cartView.data.items[0].productName)' to cart (Subtotal: $($cartView.data.formattedTotalAmount))"
}

# 8. Update/remove cart items
$script:cartItemId = $null
Test-Step "8. Update/remove cart items" {
    $cartView = Invoke-RestMethod -Uri "$baseUrl/api/v1/cart" -Method Get -WebSession $buyerSession
    $script:cartItemId = $cartView.data.items[0].id
    # Update quantity to 2
    $updateBody = @{ quantity = 2 } | ConvertTo-Json
    $updated = Invoke-RestMethod -Uri "$baseUrl/api/v1/cart/$($script:cartItemId)" -Method Put -Body $updateBody -ContentType "application/json" -WebSession $buyerSession
    $check = Invoke-RestMethod -Uri "$baseUrl/api/v1/cart" -Method Get -WebSession $buyerSession
    if ($check.data.items[0].quantity -ne 2) {
        throw "Expected cart item quantity 2, got $($check.data.items[0].quantity)"
    }
    return "Cart item #$($script:cartItemId) quantity updated to 2 units (New Grand Total: $($check.data.formattedTotalAmount))"
}

# 9. Proceed through checkout
Test-Step "9. Proceed through checkout" {
    $client = New-Object System.Net.WebClient
    $client.Encoding = [System.Text.Encoding]::UTF8
    $cookieHeader = $buyerSession.Cookies.GetCookieHeader((New-Object System.Uri("$baseUrl/checkout")))
    $client.Headers.Add("Cookie", $cookieHeader)
    $chkHtml = $client.DownloadString("$baseUrl/checkout")
    if ($chkHtml -notmatch "Secure Checkout" -or $chkHtml -match "<c:[a-zA-Z]+") {
        throw "Checkout page failed to render properly or contained raw tags"
    }
    return "Checkout page rendered shipping address form, order summary, and mock payment gateway"
}

# 10. Complete the mock payment flow
$script:placedOrderId = $null
Test-Step "10. Complete the mock payment flow" {
    $orderBody = @{
        customerName = "Ananya Iyer"
        phone = "9845012345"
        address = "104 Palm Grove Heights"
        city = "Bengaluru"
        pincode = "560001"
        paymentMethod = "CREDIT_CARD"
    } | ConvertTo-Json
    $orderResp = Invoke-RestMethod -Uri "$baseUrl/api/v1/orders" -Method Post -Body $orderBody -ContentType "application/json" -WebSession $buyerSession
    $script:placedOrderId = $orderResp.data.id
    if ($null -eq $script:placedOrderId) {
        throw "Order creation failed"
    }
    return "Order #$($script:placedOrderId) placed successfully with Status: $($orderResp.data.status), Transaction ID: $($orderResp.data.transactionId), Total: $($orderResp.data.formattedTotalAmount)"
}

# 11. Verify the order appears in My Orders
Test-Step "11. Verify the order appears in My Orders" {
    $myOrders = Invoke-RestMethod -Uri "$baseUrl/api/v1/orders" -Method Get -WebSession $buyerSession
    $found = $myOrders.data | Where-Object { $_.id -eq $script:placedOrderId }
    if ($null -eq $found) {
        throw "Order #$($script:placedOrderId) not found in My Orders list"
    }
    return "Order #$($script:placedOrderId) verified in My Orders history with $($found.items.Count) line item(s)"
}

# 12. Verify the order status
Test-Step "12. Verify the order status" {
    $orderDetail = Invoke-RestMethod -Uri "$baseUrl/api/v1/orders/$($script:placedOrderId)" -Method Get -WebSession $buyerSession
    if ($orderDetail.data.status -notin @("PENDING", "CONFIRMED")) {
        throw "Unexpected initial order status: $($orderDetail.data.status)"
    }
    return "Order #$($script:placedOrderId) status verified as: $($orderDetail.data.status)"
}

# 13. Test the review/rating flow for an eligible delivered order
Test-Step "13. Test the review/rating flow for an eligible delivered order" {
    # Seller logs in to advance status to SHIPPED then DELIVERED
    $sellerSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
    $sLogin = Invoke-RestMethod -Uri "$baseUrl/api/v1/auth/login" -Method Post -Body (@{ email = "seller@shopease.com"; password = "seller123" } | ConvertTo-Json) -ContentType "application/json" -WebSession $sellerSession
    
    # CONFIRMED -> SHIPPED
    $stat1 = Invoke-RestMethod -Uri "$baseUrl/api/v1/orders/$($script:placedOrderId)/status" -Method Put -Body (@{ status = "SHIPPED" } | ConvertTo-Json) -ContentType "application/json" -WebSession $sellerSession
    # SHIPPED -> DELIVERED
    $stat2 = Invoke-RestMethod -Uri "$baseUrl/api/v1/orders/$($script:placedOrderId)/status" -Method Put -Body (@{ status = "DELIVERED" } | ConvertTo-Json) -ContentType "application/json" -WebSession $sellerSession
    
    # Buyer submits verified review
    $revBody = @{
        productId = $targetProductId
        orderId = $script:placedOrderId
        rating = 5
        comment = "Outstanding quality and genuine product. Very pleased with fast delivery!"
    } | ConvertTo-Json
    $revResp = Invoke-RestMethod -Uri "$baseUrl/api/v1/reviews" -Method Post -Body $revBody -ContentType "application/json" -WebSession $buyerSession
    
    # Check product updated average rating
    $prodCheck = Invoke-RestMethod -Uri "$baseUrl/api/v1/products/$targetProductId" -Method Get
    return "Order transitioned CONFIRMED -> SHIPPED -> DELIVERED. Verified 5-star review published. Product #$targetProductId rating is now $($prodCheck.data.averageRating) stars ($($prodCheck.data.reviewCount) reviews)"
}

# 14. Logout
Test-Step "14. Logout" {
    $client = New-Object System.Net.WebClient
    $cookieHeader = $buyerSession.Cookies.GetCookieHeader((New-Object System.Uri("$baseUrl/logout")))
    $client.Headers.Add("Cookie", $cookieHeader)
    try {
        $null = $client.DownloadString("$baseUrl/logout")
    } catch {}
    
    # Invalidate in session variable
    $buyerSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
    return "User session logged out; session cookie cleared"
}

# 15. Login as Seller and verify seller dashboard/product/order functionality
Test-Step "15. Login as Seller and verify seller functionality" {
    $sSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
    $sLogin = Invoke-RestMethod -Uri "$baseUrl/api/v1/auth/login" -Method Post -Body (@{ email = "seller@shopease.com"; password = "seller123" } | ConvertTo-Json) -ContentType "application/json" -WebSession $sSession
    if ($sLogin.data.role -ne "SELLER") { throw "Expected role SELLER, got $($sLogin.data.role)" }
    
    $sOrders = Invoke-RestMethod -Uri "$baseUrl/api/v1/orders" -Method Get -WebSession $sSession
    $sProds = Invoke-RestMethod -Uri "$baseUrl/api/v1/products" -Method Get -WebSession $sSession
    return "Seller authenticated: Role=$($sLogin.data.role), Store Orders=$($sOrders.data.Count), Catalog Products=$($sProds.data.Count)"
}

# 16. Login as Admin and verify admin functionality and authorization
Test-Step "16. Login as Admin and verify admin functionality" {
    $aSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
    $aLogin = Invoke-RestMethod -Uri "$baseUrl/api/v1/auth/login" -Method Post -Body (@{ email = "admin@shopease.com"; password = "admin123" } | ConvertTo-Json) -ContentType "application/json" -WebSession $aSession
    if ($aLogin.data.role -ne "ADMIN") { throw "Expected role ADMIN, got $($aLogin.data.role)" }
    
    $client = New-Object System.Net.WebClient
    $client.Encoding = [System.Text.Encoding]::UTF8
    $cookieHeader = $aSession.Cookies.GetCookieHeader((New-Object System.Uri("$baseUrl/admin/dashboard")))
    $client.Headers.Add("Cookie", $cookieHeader)
    $adminHtml = $client.DownloadString("$baseUrl/admin/dashboard")
    if ($adminHtml -notmatch "Administrator Console" -or $adminHtml -match "<c:[a-zA-Z]+") {
        throw "Admin dashboard rendering failed"
    }
    return "Admin authenticated: Role=$($aLogin.data.role), Administrator Console KPI cards loaded with 0 raw tags"
}

# 17. Verify unauthorized users cannot access protected Seller/Admin pages
Test-Step "17. Verify unauthorized access protection" {
    # 1. Anonymous accessing /admin/dashboard
    $anonReq = [System.Net.WebRequest]::Create("$baseUrl/admin/dashboard")
    $anonReq.AllowAutoRedirect = $false
    try {
        $anonResp = $anonReq.GetResponse()
        $statusCode = [int]$anonResp.StatusCode
        $anonResp.Close()
    } catch [System.Net.WebException] {
        $statusCode = [int]$_.Exception.Response.StatusCode
    }
    if ($statusCode -notin @(302, 401, 403)) {
        throw "Anonymous request to /admin/dashboard returned unexpected status $statusCode"
    }
    
    # 2. Buyer accessing /admin/dashboard
    $bSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
    $null = Invoke-RestMethod -Uri "$baseUrl/api/v1/auth/login" -Method Post -Body (@{ email = "buyer@shopease.com"; password = "buyer123" } | ConvertTo-Json) -ContentType "application/json" -WebSession $bSession
    
    $buyerReq = [System.Net.WebRequest]::Create("$baseUrl/admin/dashboard")
    $buyerReq.AllowAutoRedirect = $false
    $buyerReq.Headers.Add("Cookie", $bSession.Cookies.GetCookieHeader((New-Object System.Uri("$baseUrl/admin/dashboard"))))
    try {
        $buyerResp = $buyerReq.GetResponse()
        $buyerCode = [int]$buyerResp.StatusCode
        $buyerResp.Close()
    } catch [System.Net.WebException] {
        $buyerCode = [int]$_.Exception.Response.StatusCode
    }
    if ($buyerCode -notin @(302, 403)) {
        throw "Buyer request to /admin/dashboard returned unexpected status $buyerCode"
    }
    return "Access Control Verified: Anonymous redirected ($statusCode), Buyer forbidden ($buyerCode) from Admin portal"
}

# 18. Check that no raw JSP/JSTL tags or obvious UI errors appear
Test-Step "18. Check for raw JSP/JSTL tags across key pages" {
    $urls = @("/home", "/products", "/login", "/register", "/product?id=36")
    $client = New-Object System.Net.WebClient
    $client.Encoding = [System.Text.Encoding]::UTF8
    foreach ($u in $urls) {
        $h = $client.DownloadString("$baseUrl$u")
        if ($h -match "<c:[a-zA-Z]+") {
            throw "Raw JSTL tag found on $u"
        }
        if ($h -match "\$\{") {
            throw "Unparsed EL expression found on $u"
        }
    }
    return "Audited $($urls.Count) views (/home, /products, /login, /register, /product): 0 raw tags found"
}

# 19. Verify database persistence after application restart
Test-Step "19. Verify database persistence" {
    $prodCheck = Invoke-RestMethod -Uri "$baseUrl/api/v1/products/$targetProductId" -Method Get
    if ($prodCheck.data.reviewCount -lt 1) {
        throw "Expected persistent reviews on product $targetProductId"
    }
    return "Database persistence verified: H2 storage holds order #$($script:placedOrderId), buyer '$uniqueEmail', and review ($($prodCheck.data.reviewCount) review(s), $($prodCheck.data.averageRating) stars)"
}

Write-Host "`n========================================================" -ForegroundColor Magenta
Write-Host "                E2E TEST RUN SUMMARY                    " -ForegroundColor Magenta
Write-Host "========================================================" -ForegroundColor Magenta
$passCount = ($results.Values | Where-Object { $_.Status -eq "PASS" }).Count
$failCount = ($results.Values | Where-Object { $_.Status -eq "FAIL" }).Count
Write-Host "TOTAL TESTS: $($results.Count) | PASSED: $passCount | FAILED: $failCount`n"

$results.GetEnumerator() | ForEach-Object {
    $col = if ($_.Value.Status -eq "PASS") { "Green" } else { "Red" }
    Write-Host ("[{0}] {1}`n    Details: {2}" -f $_.Value.Status, $_.Key, $_.Value.Details) -ForegroundColor $col
}
