import { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Paper,
  TextField,
  Button,
  Alert,
} from '@mui/material';

import { useSearchParams, useNavigate } from 'react-router-dom';
import { AddPhotoAlternate } from '@mui/icons-material';

// Types for the Quiz structure
interface Hint {
  hintText: string | null;
  imageUrl: string | null;
  file?: File;
}

interface Question {
  number: number;
  questionText: string;
  answer: string;
  note: string | null;
  hints: Hint[];
}

interface QuizFormState {
  pubDate: string;
  questions: Question[];
}

export default function CreateQuiz() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const quizId = searchParams.get('id');

  const [formState, setFormState] = useState<QuizFormState>({
    pubDate: '',
    questions: Array.from({ length: 8 }, (_, i) => ({
      number: i + 1,
      questionText: '',
      answer: '',
      note: '',
      hints: Array.from({ length: (i < 4 ? 4 : 3) }, () => ({ hintText: null, imageUrl: null }))
    }))
  });

  const [feedback, setFeedback] = useState<{ severity: 'success' | 'error', message: string } | null>(null);

  useEffect(() => {
    if (quizId) {
      fetch(`/admin/quiz/${quizId}/detail`)
        .then(res => res.json())
        .then(data => {
            // Mapping existing quiz data to formState needs to happen here
            // Simplified for now, just logging the data
            console.log('Editing quiz:', data);
        });
    }
  }, [quizId]);

  const handleSubmit = async () => {
    const formData = new FormData();
    const quizData = {
      pubDate: formState.pubDate,
      questions: formState.questions
    };

    formState.questions.forEach((q, qIdx) => {
      q.hints.forEach((h, hIdx) => {
        if (h.file) {
          formData.append(`hint_image_q${qIdx + 1}_h${hIdx + 1}`, h.file);
        }
      });
    });

    formData.append('quiz', new Blob([JSON.stringify(quizData)], { type: 'application/json' }));

    const url = quizId ? `/admin/quiz/${quizId}` : '/admin/create-quiz';
    const method = quizId ? 'PUT' : 'POST';

    try {
      const res = await fetch(url, { method, body: formData });
      if (res.ok) {
        setFeedback({ severity: 'success', message: 'Quiz erfolgreich gespeichert!' });
        setTimeout(() => navigate('/admin/quizzes'), 1000);
      } else {
        setFeedback({ severity: 'error', message: 'Fehler beim Speichern.' });
      }
    } catch {
      setFeedback({ severity: 'error', message: 'Netzwerkfehler.' });
    }
  };

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        {quizId ? 'Quiz bearbeiten' : 'Neues Quiz'}
      </Typography>

      {feedback && <Alert severity={feedback.severity} sx={{ mb: 2 }}>{feedback.message}</Alert>}

      <TextField
        type="date"
        label="Veröffentlichungsdatum"
        value={formState.pubDate}
        onChange={(e) => setFormState({ ...formState, pubDate: e.target.value })}
        InputLabelProps={{ shrink: true }}
        fullWidth
        sx={{ mb: 3 }}
      />

      {formState.questions.map((q, qIdx) => (
        <Paper key={qIdx} sx={{ p: 3, mb: 3 }}>
          <Typography variant="h6">Frage {qIdx + 1}</Typography>
          <TextField
            fullWidth
            label="Fragetext"
            value={q.questionText}
            onChange={(e) => {
                const newQuestions = [...formState.questions];
                newQuestions[qIdx].questionText = e.target.value;
                setFormState({ ...formState, questions: newQuestions });
            }}
            sx={{ mt: 2, mb: 2 }}
          />
          {q.hints.map((h, hIdx) => (
            <Box key={hIdx} sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
              <TextField
                label={`Hinweis ${qIdx + 1}.${hIdx + 1}`}
                value={h.hintText || ''}
                onChange={(e) => {
                    const newQuestions = [...formState.questions];
                    newQuestions[qIdx].hints[hIdx].hintText = e.target.value;
                    setFormState({ ...formState, questions: newQuestions });
                }}
                sx={{ mr: 1, flexGrow: 1 }}
              />
              <Button component="label" variant="outlined" startIcon={<AddPhotoAlternate />}>
                Bild
                <input type="file" hidden onChange={(e) => {
                    const file = e.target.files?.[0];
                    if (file) {
                        const newQuestions = [...formState.questions];
                        newQuestions[qIdx].hints[hIdx].file = file;
                        setFormState({ ...formState, questions: newQuestions });
                    }
                }} />
              </Button>
            </Box>
          ))}
          <TextField
            fullWidth
            label="Antwort"
            value={q.answer}
            onChange={(e) => {
                const newQuestions = [...formState.questions];
                newQuestions[qIdx].answer = e.target.value;
                setFormState({ ...formState, questions: newQuestions });
            }}
            sx={{ mt: 2 }}
          />
        </Paper>
      ))}

      <Button variant="contained" onClick={handleSubmit} fullWidth sx={{ mt: 2 }}>
        Speichern
      </Button>
    </Box>
  );
}
