import { useEffect, useState } from 'react'
import axios from 'axios'
import Dice from './Dice.jsx'
import Scorecard from './Scorecard.jsx'
import GameEvents from './GameEvents.jsx'

function App() {
  const [message, setMessage] = useState('Connexion au serveur SpringBoot...')
  const [gameId, setGameId] = useState('')
  const [playerId, setPlayerId] = useState('')
  const [username, setUsername] = useState('')
  const [gameDetail, setGameDetail] = useState(null)
  const [turn, setTurn] = useState(null)
  const [selected, setSelected] = useState([])

  useEffect(() => {
    axios.get('/api/hello')
      .then(res => setMessage(res.data))
      .catch(() => setMessage('Erreur : Le serveur SpringBoot ne répond pas !'))
  }, [])

  const createGame = async () => {
    try {
      const res = await axios.post('/api/games')
      setGameId(res.data.id)
      setGameDetail(res.data)
    } catch (e) { console.error(e) }
  }

  const joinGame = async () => {
    if (!gameId || !username) return
    try {
      const res = await axios.post(`/api/games/${gameId}/join`, { username })
      setPlayerId(res.data.id)
      const gd = await axios.get(`/api/games/${gameId}`)
      setGameDetail(gd.data)
    } catch (e) { console.error(e) }
  }

  const startGame = async () => {
    if (!gameId) return
    try {
      const res = await axios.post(`/api/games/${gameId}/start`)
      setGameDetail(res.data)
    } catch (e) { console.error(e) }
  }

  const roll = async () => {
    if (!gameId) return
    try {
      const res = await axios.post(`/api/games/${gameId}/turns/roll`)
      setTurn(res.data)
      setSelected([])
    } catch (e) { console.error(e) }
  }

  const reroll = async () => {
    if (!gameId) return
    try {
      const res = await axios.post(`/api/games/${gameId}/turns/reroll`, { indices: selected })
      setTurn(res.data)
      setSelected([])
    } catch (e) { console.error(e) }
  }

  const endTurn = async () => {
    if (!gameId) return
    try {
      await axios.post(`/api/games/${gameId}/turns/end`)
      const gd = await axios.get(`/api/games/${gameId}`)
      setGameDetail(gd.data)
      setTurn(null)
      setSelected([])
    } catch (e) { console.error(e) }
  }

  const parseDice = (diceCsv) => {
    if (!diceCsv) return []
    return diceCsv.split(',').map(s => parseInt(s, 10))
  }

  const toggleSelect = (index) => {
    setSelected(prev => prev.includes(index) ? prev.filter(i => i !== index) : [...prev, index])
  }

  return (
    <div style={{ padding: 16, fontFamily: 'Arial, sans-serif' }}>
      <h1>Yams — Quick Demo</h1>
      <p>Statut du serveur : {message}</p>

      <section style={{ marginTop: 12 }}>
        <button onClick={createGame}>Create Game</button>
        <input placeholder="game id" value={gameId || ''} onChange={e => setGameId(e.target.value)} style={{ marginLeft: 8 }} />
      </section>

      <section style={{ marginTop: 12 }}>
        <input placeholder="Pseudo" value={username} onChange={e => setUsername(e.target.value)} />
        <button onClick={joinGame} style={{ marginLeft: 8 }}>Join</button>
        <div>PlayerId: {playerId}</div>
      </section>

      <section style={{ marginTop: 12 }}>
        <button onClick={startGame} disabled={!gameId}>Start Game</button>
        <button onClick={roll} disabled={!gameId} style={{ marginLeft: 8 }}>Roll</button>
        <button onClick={reroll} disabled={!gameId || !turn} style={{ marginLeft: 8 }}>Reroll Selected</button>
        <button onClick={endTurn} disabled={!gameId} style={{ marginLeft: 8 }}>End Turn</button>
      </section>

      <section style={{ marginTop: 16 }}>
        <h3>Game</h3>
        <pre>{gameDetail ? JSON.stringify(gameDetail, null, 2) : 'No game loaded'}</pre>
      </section>

      <section style={{ marginTop: 8 }}>
        <h3>Live Events</h3>
        <GameEvents gameId={gameId} />
      </section>

      <section style={{ marginTop: 16 }}>
        <h3>Current Turn</h3>
        {turn ? (
          <div>
            <div>Rolls: {turn.rolls} — Completed: {turn.completed ? 'yes' : 'no'}</div>
            <Dice dice={parseDice(turn.dice)} selected={selected} onToggle={toggleSelect} />
          </div>
        ) : (
          <div>No active turn</div>
        )}
      </section>

      <section>
        <Scorecard gameId={gameId} playerId={playerId} turn={turn} />
      </section>
    </div>
  )
}

export default App