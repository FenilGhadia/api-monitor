import api from './api'
import { formatISO } from 'date-fns'

const toIsoParam = (date) =>
  date
    ? formatISO(date, { representation: 'complete' }).slice(0, 19)
    : undefined

const logsService = {

  
  async getLogs(
    filters = {},
    page = 0,
    size = 20,
    sort = 'timestamp,desc'
  ) {

    const params = {
      page,
      size,
      sort,
    }

    

    if (filters.serviceName)
      params.serviceName = filters.serviceName

    if (filters.endpoint)
      params.endpoint = filters.endpoint

    if (filters.httpMethod)
      params.httpMethod = filters.httpMethod

    if (filters.statusCode)
      params.statusCode = filters.statusCode

    if (filters.fromDate)
      params.fromDate = toIsoParam(filters.fromDate)

    if (filters.toDate)
      params.toDate = toIsoParam(filters.toDate)

    if (filters.userId)
      params.userId = filters.userId

    if (filters.slowOnly)
      params.slowOnly = true

    if (filters.errorOnly)
      params.errorOnly = true

    const response = await api.get('/logs', { params })

    return response.data.data
  },

  
  async getLogById(id) {

    const response = await api.get(`/logs/${id}`)

    return response.data.data
  },
}

export default logsService