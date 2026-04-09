import { useEffect, useState } from 'react'
import axios from 'axios'

function App() {
  const [message, setMessage] = useState("Connexion au serveur SpringBoot...")

  useEffect(() => {
    axios.get('/api/hello')
      .then(res => setMessage(res.data))
      .catch(err => setMessage("Erreur : Le serveur SpringBoot ne répond pas !"))
  }, [])

  return (
    <div>
      <h1>Projet Yams - INP ENSEEIHT</h1>
      <p>Statut du serveur : {message}</p>
    </div>
  )
}

export default App