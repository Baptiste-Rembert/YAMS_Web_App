import React, { useEffect, useState } from 'react'
import axios from 'axios'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

export default function GameEvents({ gameId }) {
  const [events, setEvents] = useState([])
  const [history, setHistory] = useState([])

  useEffect(() => {
    if (!gameId) {
      setEvents([])
      setHistory([])
      return
    }

    axios.get(`/api/games/${gameId}/events?limit=10`)
      .then(res => setHistory(Array.isArray(res.data) ? res.data : []))
      .catch(err => console.error('Failed to load event history', err))

    const client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      reconnectDelay: 5000,
      debug: (str) => { /*console.log(str)*/ }
    })

    client.onConnect = () => {
      client.subscribe(`/topic/game.${gameId}.events`, msg => {
        try {
          const body = JSON.parse(msg.body)
          setEvents(prev => [body, ...prev].slice(0, 50))
        } catch (e) {
          console.error('Invalid event format', e)
        }
      })
    }

    client.onStompError = (frame) => {
      console.error('Broker error', frame)
    }

    client.activate()
    return () => {
      try { client.deactivate() } catch (e) { /* ignore */ }
    }
  }, [gameId])

  return (
    <div style={{ marginTop: 8, maxHeight: 220, overflow: 'auto', background: '#fafafa', padding: 8, borderRadius: 6 }}>
      {history.length > 0 ? (
        <>
          <div style={{ fontSize: 12, color: '#666', marginBottom: 6 }}>History</div>
          <ul style={{ listStyle: 'none', paddingLeft: 0 }}>
            {history.map((e, i) => (
              <li key={`h-${i}`} style={{ padding: '6px 4px', borderBottom: '1px solid #eee' }}>
                <div style={{ fontSize: 12, color: '#666' }}>{e.type}</div>
                <div style={{ fontSize: 14 }}>{JSON.stringify(e.data)}</div>
              </li>
            ))}
          </ul>
        </>
      ) : null}
      {events.length === 0 ? <div>No live events</div> : (
        <ul style={{ listStyle: 'none', paddingLeft: 0 }}>
          {events.map((e, i) => (
            <li key={i} style={{ padding: '6px 4px', borderBottom: '1px solid #eee' }}>
              <div style={{ fontSize: 12, color: '#666' }}>{e.type}</div>
              <div style={{ fontSize: 14 }}>{JSON.stringify(e.data)}</div>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
