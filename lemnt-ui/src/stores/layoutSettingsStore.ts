import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { ProSettings } from '@ant-design/pro-components';

export const defaultLayoutSettings: ProSettings = {
  navTheme: 'light',
  colorPrimary: '#1677ff',
  layout: 'mix',
  contentWidth: 'Fluid',
  fixedHeader: false,
  fixSiderbar: true,
  colorWeak: false,
};

interface LayoutSettingsState {
  settings: ProSettings;
}

export const useLayoutSettings = create<LayoutSettingsState>()(
  persist(
    () => ({
      settings: defaultLayoutSettings,
    }),
    {
      name: 'lemnt-layout-settings',
      version: 1,
      partialize: (state) => ({ settings: state.settings }),
    },
  ),
);
