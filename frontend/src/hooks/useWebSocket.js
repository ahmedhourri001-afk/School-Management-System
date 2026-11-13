import { useEffect, useRef, useState } from 'react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

export function useWebSocket(userEmail) {
  const [notifications, setNotifications] = useState([])
  const clientRef = useRef(null)

  useEffect(() => {
    if (!userEmail) return

    const client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      onConnect: () => {
        // Subscribe to personal notification queue
        client.subscribe(`/user/${userEmail}/queue/notifications`, (msg) => {
          const notification = JSON.parse(msg.body)
          setNotifications(prev => [notification, ...prev])
        })
      },
      reconnectDelay: 5000,
    })

    client.activate()
    clientRef.current = client

    return () => {
      if (clientRef.current) clientRef.current.deactivate()
    }
  }, [userEmail])

  const clearNotifications = () => setNotifications([])

  return { notifications, clearNotifications }
}
