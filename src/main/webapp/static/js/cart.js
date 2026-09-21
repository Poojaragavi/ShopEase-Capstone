/**
 * Cart AJAX operations for ShopEase
 */
async function addProductToCart(productId, quantity = 1) {
    try {
        const resp = await fetch(getContextPath() + '/api/v1/cart', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ productId, quantity })
        });

        const result = await resp.json();
        if (resp.status === 401) {
            window.location.href = getContextPath() + '/login?redirect=' + encodeURIComponent(window.location.pathname);
            return;
        }

        if (result.success) {
            showToast('Item added to cart! 🛒');
            updateCartBadge();
        } else {
            showToast(result.error ? result.error.message : 'Failed to add item.', true);
        }
    } catch (e) {
        showToast('Network error while updating cart.', true);
    }
}

async function updateCartBadge() {
    try {
        const resp = await fetch(getContextPath() + '/api/v1/cart');
        if (resp.ok) {
            const data = await resp.json();
            if (data.success && data.data) {
                const badge = document.querySelector('.badge-cart');
                if (badge) {
                    badge.innerText = data.data.totalItemCount || 0;
                }
            }
        }
    } catch (ignored) {
    }
}

function getContextPath() {
    return window.location.pathname.substring(0, window.location.pathname.indexOf('/', 2)) === '/shopease' ? '/shopease' : '';
}
