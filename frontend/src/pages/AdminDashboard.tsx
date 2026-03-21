import {
  Grid,
  Paper,
  Typography,
  Box,
  Button,
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import {
  Quiz as QuizIcon,
  Groups as TeamIcon,
  AddCircle as ResultIcon,
  PersonAdd as UserIcon,
  Description as ExportIcon,
} from '@mui/icons-material';
import { cloneElement, type ReactElement } from 'react';

export default function AdminDashboard() {
  const navigate = useNavigate();

  const actions = [
    { text: 'Neues Quiz', icon: <QuizIcon />, onClick: () => navigate('/admin/create-quiz'), color: 'primary' },
    { text: 'Alle Quizze', icon: <QuizIcon />, onClick: () => navigate('/admin/quizzes'), color: 'secondary' },
    { text: 'Teams verwalten', icon: <TeamIcon />, onClick: () => navigate('/admin/teams'), color: 'primary' },
    { text: 'Ergebnis hinzufügen', icon: <ResultIcon />, onClick: () => navigate('/admin/add-result'), color: 'success' },
    { text: 'Benutzer verwalten', icon: <UserIcon />, onClick: () => navigate('/admin/register-user'), color: 'info' },
    { text: 'Exportieren', icon: <ExportIcon />, onClick: () => window.open('/admin/results/export', '_blank'), color: 'warning' },
  ];

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        📊 Admin Bereich
      </Typography>

      <Grid container spacing={3}>
        {actions.map((action) => (
          <Grid size={{ xs: 12, sm: 6, md: 4 }} key={action.text}>
            <Paper
              sx={{
                p: 3,
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                textAlign: 'center',
                height: '100%',
              }}
            >
              <Box sx={{ color: `${action.color}.main`, mb: 2 }}>
                {cloneElement(action.icon as ReactElement, { 
                  // @ts-expect-error - Icon might not have sx prop but MUI icons do
                  sx: { fontSize: 40 } 
                })}
              </Box>
              <Typography variant="h6" gutterBottom>
                {action.text}
              </Typography>
              <Button
                variant="contained"
                color={action.color as any}
                onClick={action.onClick}
                fullWidth
                sx={{ mt: 'auto' }}
              >
                Ausführen
              </Button>
            </Paper>
          </Grid>
        ))}
      </Grid>
    </Box>
  );
}
