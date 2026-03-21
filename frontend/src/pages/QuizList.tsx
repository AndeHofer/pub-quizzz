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
  Button,
  IconButton,
} from '@mui/material';
import { Delete as DeleteIcon, Edit as EditIcon, Add as AddIcon } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import type { QuizDTO } from '../types';

export default function QuizList() {
  const [quizzes, setQuizzes] = useState<QuizDTO[]>([]);
  const navigate = useNavigate();

  useEffect(() => {
    fetchQuizzes();
  }, []);

  const fetchQuizzes = () => {
    fetch('/admin/quizzes')
      .then(res => res.json())
      .then(setQuizzes);
  };

  const deleteQuiz = async (quizId: number) => {
    if (confirm(`Quiz ${quizId} wirklich löschen?`)) {
      await fetch(`/admin/quiz/${quizId}`, { method: 'DELETE' });
      fetchQuizzes();
    }
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
        <Typography variant="h4">Quizze</Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => navigate('/admin/create-quiz')}
        >
          Neues Quiz
        </Button>
      </Box>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>ID</TableCell>
              <TableCell>Veröffentlicht am</TableCell>
              <TableCell>Abgabedatum</TableCell>
              <TableCell>Fragen</TableCell>
              <TableCell align="right">Aktionen</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {quizzes.map((q) => (
              <TableRow key={q.quizId}>
                <TableCell>{q.quizId}</TableCell>
                <TableCell>{q.pubDate}</TableCell>
                <TableCell>{q.submitDate}</TableCell>
                <TableCell>{q.questionCount}</TableCell>
                <TableCell align="right">
                  <IconButton onClick={() => navigate(`/admin/create-quiz?id=${q.quizId}`)}>
                    <EditIcon />
                  </IconButton>
                  <IconButton color="error" onClick={() => deleteQuiz(q.quizId)}>
                    <DeleteIcon />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
}
