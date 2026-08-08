package com.akademi.finsight.integration.infina.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InfinaEndpoints {

	public static final String BENCHMARK_INFO = "/BenchmarkInfo";
	public static final String FUND_INFO = "/FonKunye.v2";
	public static final String FUND_PORTFOLIO_ALLOCATION = "/FundPortfolioAllocation";
	public static final String FX_PRICE = "/DovizFiyat";
	public static final String INDEX_PRICE = "/EndeksFiyat";
	public static final String ECONOMIC_PRICE = "/EkonomikFiyat";
	public static final String FUND_DAILY_RETURN = "/FonGunlukGetiri";
	// 403 — erişim henüz açılmadı. Açılınca interpolateBenchmarkDailyYields kalkacak, gerçek günlük benchmark + fon getirisi gelecek.
	public static final String CUMULATIVE_RETURNS = "/CumulativeReturns";

}
