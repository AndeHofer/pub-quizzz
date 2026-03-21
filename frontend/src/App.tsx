import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { UserProvider } from './context/UserContext';
import ProtectedRoute from './components/ProtectedRoute';
import MainLayout from './components/MainLayout';
import Leaderboard from './pages/Leaderboard';
import AdminDashboard from './pages/AdminDashboard';
import AddResult from './pages/AddResult';
import QuizList from './pages/QuizList';
import TeamList from './pages/TeamList';
import RegisterUser from './pages/RegisterUser';
import CreateQuiz from './pages/CreateQuiz';

// Create a professional MUI theme
const theme = createTheme({
  palette: {
    primary: {
      main: '#4CAF50', // Matching your existing green
    },
    secondary: {
      main: '#2196F3', // Matching your existing blue
    },
    background: {
      default: '#f4f4f4',
    },
  },
  typography: {
    h4: {
      fontWeight: 600,
    },
    h6: {
      fontWeight: 500,
    },
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          textTransform: 'none',
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          boxShadow: '0 2px 4px rgba(0, 0, 0, 0.1)',
        },
      },
    },
  },
});

function App() {
  return (
    <ThemeProvider theme={theme}>
      <Router>
        <UserProvider>
          <MainLayout>
            <Routes>
              {/* Public Routes */}
              <Route path="/" element={<Navigate to="/leaderboard" replace />} />
              <Route path="/leaderboard" element={<Leaderboard />} />

              {/* Admin Routes */}
              <Route path="/admin" element={<ProtectedRoute><AdminDashboard /></ProtectedRoute>} />
              <Route path="/admin/create-quiz" element={<ProtectedRoute><CreateQuiz /></ProtectedRoute>} />
              <Route path="/admin/quizzes" element={<ProtectedRoute><QuizList /></ProtectedRoute>} />
              <Route path="/admin/teams" element={<ProtectedRoute><TeamList /></ProtectedRoute>} />
              <Route path="/admin/add-result" element={<ProtectedRoute><AddResult /></ProtectedRoute>} />
              <Route path="/admin/register-user" element={<ProtectedRoute><RegisterUser /></ProtectedRoute>} />
              
              {/* Fallback */}
              <Route path="*" element={<Navigate to="/leaderboard" replace />} />
            </Routes>
          </MainLayout>
        </UserProvider>
      </Router>
    </ThemeProvider>
  );
}


export default App;
