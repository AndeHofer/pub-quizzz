import {beforeEach, describe, expect, it, vi} from 'vitest';
import {wireLogoutButton} from './index';

const triggerReloginMock = vi.fn(async () => {
});

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

        wireLogoutButton(fakeDocument, triggerReloginMock);

        const preventDefault = vi.fn();
        await listeners.click({preventDefault} as unknown as Event);

        expect(addEventListener).toHaveBeenCalledTimes(1);
        expect(triggerReloginMock).toHaveBeenCalledTimes(1);
        expect(preventDefault).toHaveBeenCalledTimes(1);
    });

    it('does nothing when #logoutBtn is absent', () => {
        const getElementById = vi.fn(() => null);
        const fakeDocument = {
            getElementById
        } as unknown as Document;

        wireLogoutButton(fakeDocument, triggerReloginMock);

        expect(getElementById).toHaveBeenCalledWith('logoutBtn');
        expect(triggerReloginMock).not.toHaveBeenCalled();
    });
});
