import React from 'react'

export default function Dice({ dice = [], selected = [], onToggle }) {
  return (
    <div style={{ display: 'flex', gap: 8, marginTop: 8 }}>
      {dice.map((d, i) => (
        <button
          key={i}
          onClick={() => onToggle(i)}
          style={{
            width: 56,
            height: 56,
            fontSize: 20,
            background: selected.includes(i) ? '#ffd54f' : '#eee',
            border: '1px solid #ccc',
            borderRadius: 6,
            cursor: 'pointer'
          }}
        >
          {d}
        </button>
      ))}
    </div>
  )
}
