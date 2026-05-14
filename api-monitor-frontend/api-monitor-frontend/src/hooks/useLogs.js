import { useState, useEffect, useCallback, useRef } from 'react'
import logsService from '../services/logsService'

export function useLogs(pageSize = 20) {
  const [page, setPageData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [currentPage, setCurrentPage] = useState(0)
  const [filters, setFilters] = useState({})

  const mountedRef = useRef(true)

  useEffect(() => {
    mountedRef.current = true
    return () => { mountedRef.current = false }
  }, [])

  
  const fetchLogs = useCallback(async (pageIndex, activeFilters) => {
    if (!mountedRef.current) return

    setLoading(true)
    setError(null)

    try {
      const result = await logsService.getLogs(
        activeFilters,
        pageIndex,
        pageSize,
        'timestamp,desc'
      )

      if (!mountedRef.current) return
      setPageData(result)

    } catch (err) {
      if (!mountedRef.current) return

      setError(
        err.response?.data?.message ||
        err.message ||
        'Failed to load logs'
      )
    } finally {
      if (mountedRef.current) setLoading(false)
    }
  }, [pageSize])

 
  useEffect(() => {
    fetchLogs(currentPage, filters)
  }, [currentPage])

  
  const setPage = useCallback((n) => {
    setCurrentPage(n)
  }, [])

  
  const applyFilters = useCallback((newFilters) => {
    setFilters(newFilters)
    setCurrentPage(0)
    fetchLogs(0, newFilters) 
  }, [fetchLogs])

  
  const refresh = useCallback(() => {
    fetchLogs(currentPage, filters)
  }, [currentPage, filters, fetchLogs])

  return {
    page,
    loading,
    error,
    currentPage,
    filters,
    setPage,
    applyFilters,
    refresh,
  }
}