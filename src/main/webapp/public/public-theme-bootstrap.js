(() => {
    const root = document.documentElement;
    root.dataset.theme = 'standard';
    try {
        const themes = 'standard,january,february,march,april,may,june,july,august,september,october,november,december,easter,halloween,christmas,new-year'.split(',');
        const preview = sessionStorage.getItem('pub-quizzz.theme-preview');
        const resolved = sessionStorage.getItem('pub-quizzz.resolved-theme');
        root.dataset.theme = preview && preview !== 'standard' && themes.includes(preview)
            ? preview : resolved && themes.includes(resolved) ? resolved : 'standard';
        const motif = sessionStorage.getItem('pub-quizzz.motif-layout');
        if (motif && /^[1-4]$/.test(motif)) root.dataset.motifLayout = motif;
    } catch {
        // Use the neutral first-paint fallback when browser storage is unavailable.
    }
})();
