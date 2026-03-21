import { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Paper,
  TextField,
  MenuItem,
  Button,
  Grid,
  Alert,
} from '@mui/material';
import type { QuizDTO, TeamDTO, CreateResultRequest } from '../types';

export default function AddResult() {
  const [quizzes, setQuizzes] = useState<QuizDTO[]>([]);
  const [teams, setTeams] = useState<TeamDTO[]>([]);
  
  const [quizId, setQuizId] = useState('');
  const [teamId, setTeamId] = useState('');
  const [points, setPoints] = useState<number[]>(Array(8).fill(0));
  
  const [feedback, setFeedback] = useState<{ severity: 'success' | 'error', message: string } | null>(null);

  useEffect(() => {
    fetch('/admin/quizzes').then(res => res.json()).then(setQuizzes);
    fetch('/admin/teams').then(res => res.json()).then(setTeams);
  }, []);

  const handlePointChange = (index: number, value: number) => {
    const newPoints = [...points];
    newPoints[index] = Math.max(0, value);
    setPoints(newPoints);
  };

  const handleSubmit = async () => {
    if (!quizId || !teamId) {
      setFeedback({ severity: 'error', message: 'Bitte Quiz und Team auswählen.' });
      return;
    }

    const payload: CreateResultRequest = {
      quizId: Number(quizId),
      teamId: Number(teamId),
      answers: points.map((p, i) => ({ questionNumber: i + 1, points: p })),
    };

    try {
      const res = await fetch('/admin/results', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (res.ok) {
        setFeedback({ severity: 'success', message: 'Ergebnis erfolgreich gespeichert!' });
        setQuizId('');
        setTeamId('');
        setPoints(Array(8).fill(0));
      } else {
        const text = await res.text();
        setFeedback({ severity: 'error', message: 'Fehler: ' + text });
      }
    } catch (err) {
      setFeedback({ severity: 'error', message: 'Netzwerkfehler beim Speichern.' });
    }
  };

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        ➕ Ergebnis hinzufügen
      </Typography>

      <Paper sx={{ p: 3, maxWidth: 600 }}>
        {feedback && (
          <Alert severity={feedback.severity} sx={{ mb: 2 }}>{feedback.message}</Alert>
        )}

        <TextField
          select
          fullWidth
          label="Quiz auswählen"
          value={quizId}
          onChange={(e) => setQuizId(e.target.value)}
          sx={{ mb: 2 }}
        >
          {quizzes.map((q) => (
            <MenuItem key={q.quizId} value={q.quizId}>ID {q.quizId} — {q.pubDate}</MenuItem>
          ))}
        </TextField>

        <TextField
          select
          fullWidth
          label="Team auswählen"
          value={teamId}
          onChange={(e) => setTeamId(e.target.value)}
          sx={{ mb: 2 }}
        >
          {teams.map((t) => (
            <MenuItem key={t.teamsId} value={t.teamsId}>{t.teamName}</MenuItem>
          ))}
        </TextField>

        <Grid container spacing={2}>
          {points.map((p, i) => (
            <Grid size={{ xs: 6 }} key={i}>
              <TextField
                type="number"
                label={`Frage ${i + 1} Punkte`}
                value={p}
                onChange={(e) => handlePointChange(i, Number(e.target.value))}
                fullWidth
                inputProps={{ min: 0 }}
              />
            </Grid>
          ))}
        </Grid>

        <Button
          variant="contained"
          onClick={handleSubmit}
          sx={{ mt: 3 }}
          fullWidth
        >
          Speichern
        </Button>
      </Paper>
    </Box>
  );
}
