import { useEffect, useState } from 'react';
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Typography,
  Box,
  TextField,
  MenuItem,
} from '@mui/material';
import type { LeaderboardEntry, QuizDTO } from '../types';

export default function Leaderboard() {
  const [leaderboard, setLeaderboard] = useState<LeaderboardEntry[]>([]);
  const [quizzes, setQuizzes] = useState<QuizDTO[]>([]);
  const [selectedQuizId, setSelectedQuizId] = useState<string>('');

  useEffect(() => {
    fetch('/admin/quizzes')
      .then(res => res.json())
      .then(setQuizzes);
  }, []);

  useEffect(() => {
    const url = selectedQuizId ? `/admin/leaderboard?quizId=${selectedQuizId}` : '/admin/leaderboard';
    fetch(url)
      .then(res => res.json())
      .then(setLeaderboard);
  }, [selectedQuizId]);

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        🏆 Rangliste
      </Typography>

      <TextField
        select
        label="Quiz auswählen"
        value={selectedQuizId}
        onChange={(e) => setSelectedQuizId(e.target.value)}
        sx={{ mb: 3, minWidth: 200 }}
      >
        <MenuItem value="">Alle Quizze</MenuItem>
        {quizzes.map((q) => (
          <MenuItem key={q.quizId} value={q.quizId}>
            Quiz {q.quizId} ({q.pubDate})
          </MenuItem>
        ))}
      </TextField>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Rang</TableCell>
              <TableCell>Team</TableCell>
              <TableCell>Datum</TableCell>
              <TableCell align="right">Punkte</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {leaderboard.map((entry) => (
              <TableRow key={`${entry.teamId}-${entry.quizId}`}>
                <TableCell>
                  {entry.rank === 1 ? '🥇' : entry.rank === 2 ? '🥈' : entry.rank === 3 ? '🥉' : entry.rank}
                </TableCell>
                <TableCell>{entry.teamName}</TableCell>
                <TableCell>{entry.quizDate}</TableCell>
                <TableCell align="right">
                  <strong>{entry.totalPoints}</strong>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
}
