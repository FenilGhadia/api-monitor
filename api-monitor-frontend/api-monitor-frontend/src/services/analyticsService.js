import api from './api'
import { formatISO } from 'date-fns'


const toIsoParam = (date) =>
  date ? formatISO(date, { representation: 'complete' }).slice(0, 19) : undefined

const timeParams = (from, to) => {
  const params = {}
  if (from) params.from = toIsoParam(from)
  if (to)   params.to   = toIsoParam(to)
  return params
}

const analyticsService = {
  
  async getSummary(from = null, to = null) {
    const response = await api.get('/analytics/summary', { params: timeParams(from, to) })
    return response.data.data
  },

  
  async getHourlyTimeSeries(from = null, to = null) {
    const response = await api.get('/analytics/timeseries/hourly', { params: timeParams(from, to) })
    return response.data.data
  },

 
  async getDailyTimeSeries(from = null, to = null) {
    const response = await api.get('/analytics/timeseries/daily', { params: timeParams(from, to) })
    return response.data.data
  },

  
  async getStatusCodeDistribution(from = null, to = null) {
    const response = await api.get('/analytics/status-codes', { params: timeParams(from, to) })
    return response.data.data
  },

  
  async getEndpointStats(from = null, to = null) {
    const response = await api.get('/analytics/endpoints', { params: timeParams(from, to) })
    return response.data.data
  },

  
  async getTopEndpoints(from = null, to = null, limit = 10) {
    const response = await api.get('/analytics/endpoints/top', {
      params: { ...timeParams(from, to), limit }
    })
    return response.data.data
  },

  
  async getSlowestEndpoints(from = null, to = null, limit = 10) {
    const response = await api.get('/analytics/endpoints/slowest', {
      params: { ...timeParams(from, to), limit }
    })
    return response.data.data
  },
}

export default analyticsService