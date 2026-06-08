import type {apiFetch} from './admin-api';

type ApiFetchFunction = typeof apiFetch;

type ApiFetchLoader = () => Promise<{ apiFetch: ApiFetchFunction }>;

let cachedApiFetchPromise: Promise<ApiFetchFunction> | null = null;

function resolveApiFetch(loader: ApiFetchLoader): Promise<ApiFetchFunction> {
    if (!cachedApiFetchPromise) {
        cachedApiFetchPromise = loader().then(module => module.apiFetch);
    }
    return cachedApiFetchPromise;
}

export async function getApiFetch(): Promise<ApiFetchFunction> {
    return resolveApiFetch(() => import('./admin-api'));
}
