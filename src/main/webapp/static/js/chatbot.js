/**
 * AI Chatbot widget logic interacting with POST /api/v1/chat
 */
document.addEventListener('DOMContentLoaded', () => {
    const toggler = document.getElementById('chatbotToggler');
    const container = document.getElementById('chatbotContainer');
    const closeBtn = document.getElementById('chatbotClose');
    const sendBtn = document.getElementById('chatbotSend');
    const input = document.getElementById('chatbotInput');
    const messages = document.getElementById('chatbotMessages');

    if (!toggler || !container) return;

    toggler.addEventListener('click', () => {
        container.classList.toggle('hidden');
        if (!container.classList.contains('hidden')) {
            input.focus();
        }
    });

    closeBtn.addEventListener('click', () => {
        container.classList.add('hidden');
    });

    sendBtn.addEventListener('click', sendMessage);
    input.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            sendMessage();
        }
    });

    async function sendMessage() {
        const text = input.value.trim();
        if (!text) return;

        appendMessage(text, 'user');
        input.value = '';
        input.disabled = true;
        sendBtn.disabled = true;

        const loadingMsg = appendMessage('Thinking...', 'bot');

        try {
            const contextPath = getChatContextPath();
            const resp = await fetch(contextPath + '/api/v1/chat', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ message: text })
            });

            const data = await resp.json();
            loadingMsg.remove();

            if (data.success && data.data && data.data.reply) {
                appendMessage(data.data.reply, 'bot');
            } else {
                appendMessage(data.error ? data.error.message : 'Sorry, could not process your question.', 'bot');
            }
        } catch (err) {
            loadingMsg.remove();
            appendMessage('Error contacting AI assistant. Please try again.', 'bot');
        } finally {
            input.disabled = false;
            sendBtn.disabled = false;
            input.focus();
        }
    }

    function appendMessage(text, sender) {
        const div = document.createElement('div');
        div.className = `chat-msg ${sender}`;
        div.innerText = text;
        messages.appendChild(div);
        messages.scrollTop = messages.scrollHeight;
        return div;
    }

    function getChatContextPath() {
        return window.location.pathname.substring(0, window.location.pathname.indexOf('/', 2)) === '/shopease' ? '/shopease' : '';
    }
});
