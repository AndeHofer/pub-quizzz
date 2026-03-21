import { createContext, useContext, useState, useEffect, type ReactNode } from 'react';

interface UserContextType {
  isAuthenticated: boolean;
  checkAuth: () => Promise<void>;
}

const UserContext = createContext<UserContextType | undefined>(undefined);

export const UserProvider = ({ children }: { children: ReactNode }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  const checkAuth = async () => {
    try {
      const res = await fetch('/user/me');
      setIsAuthenticated(res.ok);
      if (!res.ok) {
        // Not authenticated, redirect to login page provided by Spring Security
        window.location.href = '/login';
      }
    } catch {
      window.location.href = '/login';
    }
  };

  useEffect(() => {
    checkAuth();
  }, []);

  return (
    <UserContext.Provider value={{ isAuthenticated, checkAuth }}>
      {children}
    </UserContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(UserContext);
  if (!context) throw new Error('useAuth must be used within UserProvider');
  return context;
};
