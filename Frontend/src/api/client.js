import axios from 'axios';
import toast from 'react-hot-toast';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '';

const client = axios.create({
  baseURL: `${API_BASE_URL}/api/v1`,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 15000,
});

client.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      const data = error.response.data;
      const message = data?.message || `Request failed with status ${error.response.status}`;

      if (error.response.status === 409) {
        toast.error(message, { id: 'conflict-error' });
      } else if (error.response.status >= 500) {
        toast.error('Server error. Please try again.', { id: 'server-error' });
      } else if (error.response.status === 400) {
        toast.error(message || 'Validation error.', { id: 'validation-error' });
      }
    } else if (error.code === 'ECONNABORTED') {
      toast.error('Request timed out. Is the backend running?', { id: 'timeout-error' });
    } else if (error.code === 'ERR_NETWORK') {
      toast.error('Cannot connect to backend. Is the server running on port 8080?', {
        id: 'network-error',
        duration: 6000,
      });
    }

    return Promise.reject(error);
  }
);

export default client;
