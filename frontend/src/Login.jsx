import { useState } from 'react'
import axios from 'axios'

export default function Login({ currentUser, onLoginSuccess, onLogout }) {
  const [username, setUsername] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const submit = async (mode) => {
    if (!username.trim()) {
      setError('Choisis un pseudo.')
      return
    }
    setLoading(true)
    setError('')
    try {
      const response = await axios.post(`/api/auth/${mode}`, { username: username.trim() })
      onLoginSuccess(response.data)
      setUsername('')
    } catch (err) {
      setError(err.response?.data || err.message)
    } finally {
      setLoading(false)
    }
  }

  if (currentUser) {
    return (
      <div className="login-container">
        <h2>Session active</h2>
        <p>Connecté en tant que <strong>{currentUser.username}</strong></p>
        <button type="button" onClick={onLogout}>Se déconnecter</button>
      </div>
    )
  }

  return (
    <div className="login-container">
      <h2>Connexion au Yams</h2>
      <p>La session est conservée côté serveur. Recharge la page sans perdre ton identité.</p>
      <input
        type="text"
        value={username}
        onChange={(e) => setUsername(e.target.value)}
        placeholder="Entrez votre pseudo"
        required
      />
      <div style={{ display: 'flex', gap: 8, marginTop: 8 }}>
        <button type="button" onClick={() => submit('login')} disabled={loading}>Se connecter</button>
        <button type="button" onClick={() => submit('register')} disabled={loading}>Créer le compte</button>
      </div>
      {error ? <p style={{ color: '#b00020' }}>{error}</p> : null}
    </div>
  )
}