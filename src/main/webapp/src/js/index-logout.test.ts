import {beforeEach, describe, expect, it, vi} from 'vitest';

vi.mock('./logout-action', () => ({
    triggerRelogin: vi.fn(async () => {
    })
}));

import {triggerRelogin} from './logout-action';

import {wireLogoutButton} from './index';

describe('index logout button', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('wires #logoutBtn click to relogin flow trigger', async () => {
        const listeners: Record<string, EventListener> = {};
        const addEventListener = vi.fn((eventName: string, handler: EventListener) => {
            listeners[eventName] = handler;
        });
        const buttonElement = {
            addEventListener
        };
        const getElementById = vi.fn((id: string) => {
            if (id === 'logoutBtn') {
                return buttonElement;
            }
            return null;
        });

        const fakeDocument = {
            getElementById
        } as unknown as Document;

        wireLogoutButton(fakeDocument);

        const preventDefault = vi.fn();
        await listeners.click({preventDefault} as unknown as Event);

        expect(addEventListener).toHaveBeenCalledTimes(1);
        expect(triggerRelogin).toHaveBeenCalledTimes(1);
        expect(preventDefault).toHaveBeenCalledTimes(1);
    });

    it('does nothing when #logoutBtn is absent', () => {
        const getElementById = vi.fn(() => null);
        const fakeDocument = {
            getElementById
        } as unknown as Document;

        wireLogoutButton(fakeDocument);

        expect(getElementById).toHaveBeenCalledWith('logoutBtn');
        expect(triggerRelogin).not.toHaveBeenCalled();
    });
});
