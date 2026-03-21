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
  TextField,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
} from '@mui/material';
import { Delete as DeleteIcon, Add as AddIcon } from '@mui/icons-material';
import type { TeamDTO } from '../types';

export default function TeamList() {
  const [teams, setTeams] = useState<TeamDTO[]>([]);
  const [open, setOpen] = useState(false);
  const [newTeamName, setNewTeamName] = useState('');

  useEffect(() => {
    fetchTeams();
  }, []);

  const fetchTeams = () => {
    fetch('/admin/teams')
      .then(res => res.json())
      .then(setTeams);
  };

  const handleCreate = async () => {
    if (!newTeamName) return;
    await fetch(`/admin/team?teamName=${encodeURIComponent(newTeamName)}`, { method: 'POST' });
    setNewTeamName('');
    setOpen(false);
    fetchTeams();
  };

  const deleteTeam = async (teamId: number, teamName: string) => {
    if (confirm(`Team "${teamName}" wirklich löschen?`)) {
      await fetch(`/admin/team/${teamId}`, { method: 'DELETE' });
      fetchTeams();
    }
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
        <Typography variant="h4">Teams</Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => setOpen(true)}
        >
          Neues Team
        </Button>
      </Box>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>ID</TableCell>
              <TableCell>Name</TableCell>
              <TableCell align="right">Aktionen</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {teams.map((t) => (
              <TableRow key={t.teamsId}>
                <TableCell>{t.teamsId}</TableCell>
                <TableCell>{t.teamName}</TableCell>
                <TableCell align="right">
                  <IconButton color="error" onClick={() => deleteTeam(t.teamsId, t.teamName)}>
                    <DeleteIcon />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog open={open} onClose={() => setOpen(false)}>
        <DialogTitle>Neues Team erstellen</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin="dense"
            label="Team-Name"
            fullWidth
            value={newTeamName}
            onChange={(e) => setNewTeamName(e.target.value)}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>Abbrechen</Button>
          <Button onClick={handleCreate} variant="contained">Erstellen</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
