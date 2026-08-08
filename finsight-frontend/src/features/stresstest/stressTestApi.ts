import axiosInstance from '@/lib/api/client'
import type { 
  ApiStandardResponse, 
  PortfolioDataDto, 
  StressTestInferenceResponseDto,
  ScenarioKey 
} from './types'

const BASE_PATH = '/stress-tests'

/**
 * Canlı Stres Testi Simülasyonunu Çalıştırır (POST /stress-tests/run)
 */
export async function runSimulation(
  fundId: string,
  simulationType: ScenarioKey,
  portfolioData: PortfolioDataDto
): Promise<StressTestInferenceResponseDto | null> {
  const response = await axiosInstance.post<ApiStandardResponse<StressTestInferenceResponseDto>>(
    `${BASE_PATH}/run`,
    portfolioData,
    {
      params: {
        fundId,
        simulationType
      }
    }
  )
  return response.data?.data ?? null
}