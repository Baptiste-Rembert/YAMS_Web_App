import React, { useEffect, useState } from 'react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

export default function GameEvents({ gameId }) {
  const [events, setEvents] = useState([])

  useEffect(() => {
    if (!gameId) return
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
      {events.length === 0 ? <div>No events</div> : (
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
