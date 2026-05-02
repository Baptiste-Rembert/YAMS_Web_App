import React, { useEffect, useState } from 'react'
import axios from 'axios'

const CATEGORIES = [
  'ONES','TWOS','THREES','FOURS','FIVES','SIXES',
  'THREE_OF_A_KIND','FOUR_OF_A_KIND','FULL_HOUSE','SMALL_STRAIGHT','LARGE_STRAIGHT','YAHTZEE','CHANCE'
]

function computeCategoryScore(dice, category) {
  const counts = [0,0,0,0,0,0,0]
  let sum = 0
  dice.forEach(d => { if (d >= 1 && d <= 6) { counts[d]++; sum += d } })

  switch (category) {
    case 'ONES': return counts[1] * 1
    case 'TWOS': return counts[2] * 2
    case 'THREES': return counts[3] * 3
    case 'FOURS': return counts[4] * 4
    case 'FIVES': return counts[5] * 5
    case 'SIXES': return counts[6] * 6
    case 'THREE_OF_A_KIND':
      for (let i = 1; i <= 6; i++) if (counts[i] >= 3) return sum
      return 0
    case 'FOUR_OF_A_KIND':
      for (let i = 1; i <= 6; i++) if (counts[i] >= 4) return sum
      return 0
    case 'FULL_HOUSE': {
      let has3 = false, has2 = false
      for (let i = 1; i <= 6; i++) { if (counts[i] === 3) has3 = true; if (counts[i] === 2) has2 = true }
      return (has3 && has2) ? 25 : 0
    }
    case 'SMALL_STRAIGHT':
      if ((counts[1] >=1 && counts[2] >=1 && counts[3] >=1 && counts[4] >=1) ||
          (counts[2] >=1 && counts[3] >=1 && counts[4] >=1 && counts[5] >=1) ||
          (counts[3] >=1 && counts[4] >=1 && counts[5] >=1 && counts[6] >=1)) return 30
      return 0
    case 'LARGE_STRAIGHT':
      if ((counts[1]===1 && counts[2]===1 && counts[3]===1 && counts[4]===1 && counts[5]===1) ||
          (counts[2]===1 && counts[3]===1 && counts[4]===1 && counts[5]===1 && counts[6]===1)) return 40
      return 0
    case 'YAHTZEE':
      for (let i = 1; i <= 6; i++) if (counts[i] === 5) return 50
      return 0
    case 'CHANCE':
      return sum
    default:
      return 0
  }
}

export default function Scorecard({ gameId, playerId, turn, onScoreMarked, canEndTurn, reloadTrigger }) {
  const [scores, setScores] = useState({})
  const [summary, setSummary] = useState(null)
  const [loading, setLoading] = useState(false)

  useEffect(() => { load(); }, [gameId, playerId, reloadTrigger])

  const load = async () => {
    if (!gameId || !playerId) return
    try {
      const resp = await axios.get(`/api/games/${gameId}/scorecard/${playerId}`)
      if (resp.data && resp.data.scores) {
        const map = JSON.parse(resp.data.scores || '{}')
        const normalized = {}
        Object.entries(map).forEach(([k,v]) => normalized[k.toUpperCase()] = v)
        setScores(normalized)
      } else setScores({})

      const sresp = await axios.get(`/api/games/${gameId}/scorecard/${playerId}/summary`)
      setSummary(sresp.data)
    } catch (e) {
      console.error(e)
    }
  }

  const submitScore = async (category, score) => {
    if (!gameId || !playerId) return
    setLoading(true)
    try {
      await axios.post(`/api/games/${gameId}/scorecard/${playerId}/score`, { category, score })
      await load()
      if (typeof onScoreMarked === 'function') onScoreMarked()
    } catch (e) {
      console.error(e)
      alert(e.response?.data || e.message)
    } finally { setLoading(false) }
  }

  const dice = turn && turn.dice ? turn.dice.split(',').map(s => parseInt(s,10)) : []

  return (
    <div style={{ marginTop: 16, borderTop: '1px solid #eee', paddingTop: 12 }}>
      <h3>Scorecard</h3>
      {(!gameId || !playerId) ? (
        <div>Join a game and start a turn to use the scorecard.</div>
      ) : (
        <div>
          <table style={{ borderCollapse: 'collapse', width: '100%' }}>
            <thead>
              <tr>
                <th style={{ textAlign: 'left' }}>Category</th>
                <th style={{ textAlign: 'left' }}>Score</th>
                <th style={{ textAlign: 'left' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {CATEGORIES.map(cat => {
                const existing = scores[cat]
                const expected = dice.length ? computeCategoryScore(dice, cat) : null
                return (
                  <tr key={cat}>
                    <td style={{ padding: '6px 8px' }}>{cat}</td>
                    <td style={{ padding: '6px 8px' }}>{existing ?? '-'}</td>
                    <td style={{ padding: '6px 8px' }}>
                      {existing == null ? (
                        <div style={{ display: 'flex', gap: 8 }}>
                          <button disabled={!dice.length || loading || canEndTurn} onClick={() => submitScore(cat, expected)}>
                            Mark {expected ?? '-'}
                          </button>
                          <button disabled={loading || canEndTurn} onClick={() => submitScore(cat, 0)}>Forfeit (0)</button>
                        </div>
                      ) : (
                        <em>Locked</em>
                      )}
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>

          <div style={{ marginTop: 12 }}>
            <strong>Summary</strong>
            {summary ? (
              <div style={{ marginTop: 8 }}>
                <div>Upper total: {summary.upperTotal} (bonus: {summary.upperBonus})</div>
                <div>Lower total: {summary.lowerTotal}</div>
                <div><strong>Total: {summary.total}</strong></div>
              </div>
            ) : (
              <div>No summary available</div>
            )}
          </div>
        </div>
      )}
    </div>
  )
}
