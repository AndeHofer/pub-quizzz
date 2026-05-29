import {defineConfig} from 'vite';
import tailwindcss from '@tailwindcss/vite';

export default defineConfig({
    root: 'src',
    publicDir: '../public',
    plugins: [
        tailwindcss(),
    ],
    build: {
        outDir: '../../resources/static',
        emptyOutDir: true,
        rollupOptions: {
            input: {
                main: './index.html',
                admin_main: './admin/admin_main.html',
                create_quiz: './admin/create_quiz.html',
                create_result: './admin/create_result.html',
                results: './admin/results.html',
                login_stats: './admin/login_stats.html',
                logs: './admin/logs.html',
                register_user: './admin/register_user.html',
                points_leaderboard: './points-leaderboard.html',
                medal_leaderboard: './medal-leaderboard.html',
                average_leaderboard: './average-leaderboard.html',
                team: './team.html',
                quizzes: './quizzes.html',
                quiz: './quiz.html',
                quiz_details: './quiz-details.html',
                rules: './rules.html'
            }
        }
    },
});
