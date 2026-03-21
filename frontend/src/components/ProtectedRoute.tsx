import { type ReactNode } from 'react';
import { useAuth } from '../context/UserContext';
import { CircularProgress, Box } from '@mui/material';

export default function ProtectedRoute({ children }: { children: ReactNode }) {
  const { isAuthenticated } = useAuth();

  if (isAuthenticated === undefined) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  // If not authenticated, the hook will handle redirect
  return isAuthenticated ? <>{children}</> : null;
}
