import { useOutletContext } from 'react-router-dom';

export const useShellActions = () => useOutletContext() || {};
