import {triggerRelogin} from './logout-action';

export {triggerRelogin};

if (typeof document !== 'undefined') {
    const reloginButton = document.getElementById('reloginBtn');
    if (reloginButton) {
        reloginButton.addEventListener('click', async (event) => {
            event.preventDefault();
            await triggerRelogin();
        });
    }
}
