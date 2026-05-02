import { useEffect, useState } from 'react'
import axios from 'axios'
import Dice from './Dice.jsx'
import Scorecard from './Scorecard.jsx'
import GameEvents from './GameEvents.jsx'
import Login from './Login.jsx'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

import { useCallback } from 'react'

function App() {
  const [message, setMessage] = useState('Connexion au serveur SpringBoot...')
  const [currentUser, setCurrentUser] = useState(null)
  const [authReady, setAuthReady] = useState(false)
  const [gameId, setGameId] = useState('')
  const [playerId, setPlayerId] = useState('')
  const [gameDetail, setGameDetail] = useState(null)
  const [turn, setTurn] = useState(null)
  const [selected, setSelected] = useState([])
  const [connectedUsers, setConnectedUsers] = useState([])
  const [availableGames, setAvailableGames] = useState([])
  const [invites, setInvites] = useState([])
  const [canEndTurn, setCanEndTurn] = useState(false)
  const [scorecardReload, setScorecardReload] = useState(0)

  useEffect(() => {
    axios.get('/api/auth/me')
      .then(res => setCurrentUser(res.data))
      .catch(() => setCurrentUser(null))
      .finally(() => setAuthReady(true))

    axios.get('/api/hello')
      .then(res => setMessage(res.data))
      .catch(() => setMessage('Erreur : Le serveur SpringBoot ne répond pas !'))
  }, [])

  const refreshAdmin = useCallback(async () => {
    try {
      const [usersResp, gamesResp] = await Promise.all([
        axios.get('/api/lobby/connected'),
        axios.get('/api/lobby/games')
      ])
      setConnectedUsers(usersResp.data || [])
      setAvailableGames(gamesResp.data || [])
    } catch (e) {
      console.error('Failed to load admin info', e)
    }
  }, [])

  useEffect(() => { refreshAdmin() }, [refreshAdmin])

  const sendInvite = async (toUsername) => {
    try {
      let targetGameId = gameId
      if (!targetGameId) {
        const res = await axios.post('/api/games')
        targetGameId = res.data.id
        setGameId(targetGameId)
        setGameDetail(res.data)
      }
      await axios.post('/api/invitations', { toUsername, gameId: targetGameId })
      // optimistic feedback
      alert(`Invitation sent to ${toUsername} for game ${targetGameId}`)
    } catch (e) {
      console.error('Failed to send invite', e)
      alert('Failed to send invite')
    }
  }

  const acceptInvite = async (inv) => {
    try {
      const res = await axios.post(`/api/games/${inv.gameId}/join`, { username: currentUser.username })
      setPlayerId(res.data.id)
      const gd = await axios.get(`/api/games/${inv.gameId}`)
      setGameDetail(gd.data)
      await refreshAdmin()
      // remove accepted invite(s)
      setInvites(prev => prev.filter(i => !(i.from === inv.from && i.gameId === inv.gameId && i.to === inv.to)))
    } catch (e) {
      console.error('Failed to accept invite', e)
      alert('Failed to accept invite')
    }
  }

  const declineInvite = (index) => {
    setInvites(prev => prev.filter((_, i) => i !== index))
  }

  useEffect(() => {
    const client = new Client({ webSocketFactory: () => new SockJS('/ws'), reconnectDelay: 5000 })
    client.onConnect = () => {
      client.subscribe('/topic/invites', msg => {
        try {
          const body = JSON.parse(msg.body)
          const data = body && body.data ? body.data : body
          if (data && data.to) {
            setInvites(prev => [data, ...prev])
          }
        } catch (e) {
          console.error('Invalid invite message', e)
        }
      })
      client.subscribe('/topic/connected', msg => {
        try {
          const body = JSON.parse(msg.body)
          const data = body && body.users ? body.users : (body && body.data && body.data.users ? body.data.users : body)
          if (Array.isArray(data)) {
            setConnectedUsers(data)
          }
        } catch (e) {
          console.error('Invalid connected message', e)
        }
      })
      // fetch initial list
      refreshAdmin()
    }
    client.activate()
    return () => { try { client.deactivate() } catch (e) {} }
  }, [])

  const handleLogout = async () => {
    try {
      await axios.post('/api/auth/logout')
    } catch (e) {
      console.error(e)
    } finally {
      setCurrentUser(null)
      setPlayerId('')
      setGameDetail(null)
      setTurn(null)
      setSelected([])
    }
  }

  const createGame = async () => {
    try {
      const res = await axios.post('/api/games')
      setGameId(res.data.id)
      setGameDetail(res.data)
      setCanEndTurn(false)
      setScorecardReload(n => n + 1)
      await refreshAdmin()
    } catch (e) { console.error(e) }
  }

  const joinGame = async () => {
    if (!gameId || !currentUser) return
    try {
      const res = await axios.post(`/api/games/${gameId}/join`, { username: currentUser.username })
      setPlayerId(res.data.id)
      const gd = await axios.get(`/api/games/${gameId}`)
      setGameDetail(gd.data)
      setCanEndTurn(false)
      await refreshAdmin()
    } catch (e) { console.error(e) }
  }

  const startOrRestartGame = async () => {
    if (!gameId) return
    try {
      let res
      if (gameDetail && gameDetail.started) {
        res = await axios.post(`/api/games/${gameId}/restart`)
      } else {
        res = await axios.post(`/api/games/${gameId}/start`)
      }
      setGameDetail(res.data)
      setCanEndTurn(false)
      setTurn(null)
      setSelected([])
      setScorecardReload(n => n + 1)
      await refreshAdmin()
    } catch (e) { console.error(e) }
  }

  const roll = async () => {
    if (!gameId || !currentUser || canEndTurn) return
    try {
      const res = await axios.post(`/api/games/${gameId}/turns/roll`)
      setTurn(res.data)
      setSelected([])
      setCanEndTurn(false)
    } catch (e) { console.error(e) }
  }

  const reroll = async () => {
    if (!gameId || canEndTurn) return
    try {
      const res = await axios.post(`/api/games/${gameId}/turns/reroll`, { indices: selected })
      setTurn(res.data)
      setSelected([])
      setCanEndTurn(false)
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
      setCanEndTurn(false)
    } catch (e) { console.error(e) }
  }

  const parseDice = (diceCsv) => {
    if (!diceCsv) return []
    return diceCsv.split(',').map(s => parseInt(s, 10))
  }

  const toggleSelect = (index) => {
    setSelected(prev => prev.includes(index) ? prev.filter(i => i !== index) : [...prev, index])
  }

  if (!authReady) {
    return <div className="app-root">Chargement de la session...</div>
  }

  return (
    <div className="app-root">
      <h1>Yams — Quick Demo</h1>
      <p className="subtitle">Statut du serveur : {message}</p>

      <div className="panel">
        <Login currentUser={currentUser} onLoginSuccess={setCurrentUser} onLogout={handleLogout} />
      </div>

      <section className="panel">
        <button onClick={createGame} disabled={!currentUser}>Create Game</button>
        <input placeholder="game id" value={gameId || ''} onChange={e => setGameId(e.target.value)} style={{ marginLeft: 8 }} />
      </section>

      <section className="panel lobby">
        <h3>Lobby</h3>
        <div>
          <strong>Connected users:</strong>
          <button onClick={refreshAdmin} style={{ marginLeft: 8 }}>Refresh</button>
        </div>
        <div style={{ marginTop: 8 }}>
          {connectedUsers.length === 0 ? (
            <div style={{ fontStyle: 'italic' }}>No users connected</div>
          ) : (
            <ul style={{ listStyle: 'none', paddingLeft: 0 }}>
              {connectedUsers.map(u => (
                <li key={u} style={{ marginBottom: 6 }}>
                  <span>{u}</span>
                  {currentUser && currentUser.username === u ? (
                    <span style={{ color: '#888', marginLeft: 8 }}>(you)</span>
                  ) : (
                    <button style={{ marginLeft: 8 }} onClick={() => sendInvite(u)}>Invite</button>
                  )}
                </li>
              ))}
            </ul>
          )}
        </div>
        <div style={{ marginTop: 8 }}>
          <strong>Available games:</strong>
          <ul>
            {availableGames.map(g => (
              <li key={g.id}>Game {g.id} — {g.started ? 'started' : 'open'}</li>
            ))}
          </ul>
        </div>

        {invites.length > 0 && (
          <div style={{ marginTop: 12 }}>
            <strong>Invitations</strong>
            <ul style={{ listStyle: 'none', paddingLeft: 0 }}>
              {invites.map((inv, i) => (
                <li key={i} style={{ marginBottom: 6 }}>
                  <span>From <strong>{inv.from}</strong> — Game {inv.gameId}</span>
                  {currentUser && currentUser.username === inv.to && (
                    <>
                      <button style={{ marginLeft: 8 }} onClick={() => acceptInvite(inv)}>Accept</button>
                      <button style={{ marginLeft: 6 }} onClick={() => declineInvite(i)}>Decline</button>
                    </>
                  )}
                </li>
              ))}
            </ul>
          </div>
        )}
      </section>

      <section className="panel">
        <button onClick={joinGame} disabled={!currentUser || !gameId} style={{ marginLeft: 8 }}>Join</button>
        <div>PlayerId: {playerId}</div>
        <div>Current user: {currentUser ? currentUser.username : 'not logged in'}</div>
      </section>

      <section className="panel">
        <h3>Game</h3>
        <pre>{gameDetail ? JSON.stringify(gameDetail, null, 2) : 'No game loaded'}</pre>
      </section>

      <section className="panel">
        <h3>Live Events</h3>
        <GameEvents gameId={gameId} />
      </section>

      <section className="panel">
        <button onClick={startOrRestartGame} disabled={!gameId || !currentUser}>{gameDetail && gameDetail.started ? 'Restart Game' : 'Start Game'}</button>
        <button onClick={roll} disabled={!gameId || !currentUser || canEndTurn} style={{ marginLeft: 8 }}>Roll</button>
        <button onClick={reroll} disabled={!gameId || !turn || !currentUser || canEndTurn} style={{ marginLeft: 8 }}>Reroll Selected</button>
        <button onClick={endTurn} disabled={!gameId || !currentUser || !canEndTurn} style={{ marginLeft: 8 }}>End Turn</button>
      </section>

      <section className="panel">
        <h3>Current Turn</h3>
        {turn && !canEndTurn ? (
          <div>
            <div>Rolls: {turn.rolls} — Completed: {turn.completed ? 'yes' : 'no'}</div>
            <Dice dice={parseDice(turn.dice)} selected={selected} onToggle={canEndTurn ? () => {} : toggleSelect} />
          </div>
        ) : (
          <div>{turn && canEndTurn ? <em>Score marked — end turn to continue</em> : 'No active turn'}</div>
        )}
      </section>

      <section className="panel scorecard">
        <Scorecard gameId={gameId} playerId={playerId} turn={turn} onScoreMarked={() => { setCanEndTurn(true); setSelected([]); }} canEndTurn={canEndTurn} reloadTrigger={scorecardReload} />
      </section>
    </div>
  )
}

export default App