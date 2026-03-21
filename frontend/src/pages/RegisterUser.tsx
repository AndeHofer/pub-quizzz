import { useState } from 'react';
import {
  Box,
  Typography,
  Paper,
  TextField,
  MenuItem,
  Button,
  Alert,
} from '@mui/material';

const ROLES = ['USER', 'ADMIN'];

export default function RegisterUser() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState('USER');
  const [feedback, setFeedback] = useState<{ severity: 'success' | 'error', message: string } | null>(null);

  const handleSubmit = async () => {
    try {
      const res = await fetch('/admin/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password, role })
      });

      if (res.ok) {
        setFeedback({ severity: 'success', message: 'Benutzer erfolgreich angelegt!' });
        setUsername('');
        setPassword('');
        setRole('USER');
      } else {
        const text = await res.text();
        setFeedback({ severity: 'error', message: 'Fehler: ' + text });
      }
    } catch (err) {
      setFeedback({ severity: 'error', message: 'Fehler beim Registrieren.' });
    }
  };

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        👤 Benutzer anlegen
      </Typography>

      <Paper sx={{ p: 3, maxWidth: 400 }}>
        {feedback && (
          <Alert severity={feedback.severity} sx={{ mb: 2 }}>{feedback.message}</Alert>
        )}

        <TextField
          fullWidth
          label="Benutzername"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          sx={{ mb: 2 }}
        />
        <TextField
          fullWidth
          type="password"
          label="Passwort"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          sx={{ mb: 2 }}
        />
        <TextField
          select
          fullWidth
          label="Rolle"
          value={role}
          onChange={(e) => setRole(e.target.value)}
          sx={{ mb: 2 }}
        >
          {ROLES.map((r) => (
            <MenuItem key={r} value={r}>{r}</MenuItem>
          ))}
        </TextField>

        <Button
          variant="contained"
          onClick={handleSubmit}
          fullWidth
        >
          Registrieren
        </Button>
      </Paper>
    </Box>
  );
}
