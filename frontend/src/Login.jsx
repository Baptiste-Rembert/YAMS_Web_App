import { useState } from 'react';
import axios from 'axios';

function Login({ onLoginSuccess }) {
  const [username, setUsername] = useState('');

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      // Appel à API SpringBoot
      const response = await axios.post('/api/auth/login', username, {
        headers: { 'Content-Type': 'text/plain' }
      });
      console.log("Joueur connecté :", response.data);
      onLoginSuccess(response.data);
    } catch (error) {
      console.error("Erreur de connexion", error);
    }
  };

  return (
    <div className="login-container">
      <h2>Connexion au Yams</h2>
      <form onSubmit={handleLogin}>
        <input 
          type="text" 
          value={username} 
          onChange={(e) => setUsername(e.target.value)} 
          placeholder="Entrez votre pseudo"
          required 
        />
        <button type="submit">Jouer</button>
      </form>
    </div>
  );
}

export default Login;