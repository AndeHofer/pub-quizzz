const TRANSITION_DURATION_MS = 80;

const markEnter = (): void => {
    document.body.classList.remove('page-leave');
    document.body.classList.add('page-enter');

    requestAnimationFrame(() => {
        document.body.classList.add('page-enter-active');
    });
};

const isExternalLink = (link: HTMLAnchorElement): boolean => {
    const url = new URL(link.href, window.location.href);
    return url.origin !== window.location.origin;
};

const shouldSkipTransition = (event: MouseEvent, link: HTMLAnchorElement): boolean => {
    const href = link.getAttribute('href');
    if (!href || href.startsWith('#')) {
        return true;
    }

    if (link.hasAttribute('download')) {
        return true;
    }

    if (link.target && link.target !== '_self') {
        return true;
    }

    if (event.metaKey || event.ctrlKey || event.shiftKey || event.altKey || event.button !== 0) {
        return true;
    }

    if (isExternalLink(link)) {
        return true;
    }

    return false;
};

const navigateWithFade = (event: MouseEvent): void => {
    const target = event.target;
    if (!(target instanceof Element)) {
        return;
    }

    const link = target.closest('a');
    if (!(link instanceof HTMLAnchorElement)) {
        return;
    }

    if (shouldSkipTransition(event, link)) {
        return;
    }

    event.preventDefault();
    document.body.classList.remove('page-enter', 'page-enter-active');
    document.body.classList.add('page-leave');

    window.setTimeout(() => {
        window.location.assign(link.href);
    }, TRANSITION_DURATION_MS);
};

document.addEventListener('DOMContentLoaded', markEnter);
window.addEventListener('pageshow', markEnter);
document.addEventListener('click', navigateWithFade);
