import { useState, useEffect, useCallback, useRef } from 'react'
import analyticsService from '../services/analyticsService'


export function useAnalytics(from = null, to = null, refreshInterval = 60_000) {
  const [summary,       setSummary]       = useState(null)
  const [timeSeries,    setTimeSeries]    = useState([])
  const [statusCodes,   setStatusCodes]   = useState([])
  const [endpointStats, setEndpointStats] = useState([])
  const [loading,       setLoading]       = useState(true)
  const [error,         setError]         = useState(null)
  const [lastUpdated,   setLastUpdated]   = useState(null)

  
  const mountedRef = useRef(true)
  useEffect(() => {
    mountedRef.current = true
    return () => { mountedRef.current = false }
  }, [])

 
  const fromRef = useRef(from)
  const toRef   = useRef(to)
  fromRef.current = from
  toRef.current   = to


  const fetchAll = useCallback(async () => {
    if (!mountedRef.current) return
    setLoading(true)
    setError(null)

    try {
     
      const [sum, ts, sc, ep] = await Promise.all([
        analyticsService.getSummary(fromRef.current, toRef.current),
        analyticsService.getHourlyTimeSeries(fromRef.current, toRef.current),
        analyticsService.getStatusCodeDistribution(fromRef.current, toRef.current),
        analyticsService.getTopEndpoints(fromRef.current, toRef.current, 10),
      ])

      if (!mountedRef.current) return
      setSummary(sum)
      setTimeSeries(ts   ?? [])
      setStatusCodes(sc  ?? [])
      setEndpointStats(ep ?? [])
      setLastUpdated(new Date())
    } catch (err) {
      if (!mountedRef.current) return
      setError(
        err.response?.data?.message ||
        err.message ||
        'Failed to load analytics data'
      )
    } finally {
      if (mountedRef.current) setLoading(false)
    }
  }, []) 

 
  const windowKey = [
    from instanceof Date ? Math.floor(from.getTime() / 60_000) : null,
    to   instanceof Date ? Math.floor(to.getTime()   / 60_000) : null,
  ].join('|')

  const prevWindowKeyRef = useRef(null)
  useEffect(() => {
    
    if (prevWindowKeyRef.current !== windowKey) {
      prevWindowKeyRef.current = windowKey
      fetchAll()
    }
  }, [windowKey, fetchAll]) 

  
  useEffect(() => {
    if (!refreshInterval) return
    const id = setInterval(fetchAll, refreshInterval)
    return () => clearInterval(id)
  }, [refreshInterval, fetchAll]) 

  return {
    summary,
    timeSeries,
    statusCodes,
    endpointStats,
    loading,
    error,
    refresh: fetchAll,
    lastUpdated,
  }
}