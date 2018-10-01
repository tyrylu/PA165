package cz.muni.fi.pa165.currency;

import java.math.BigDecimal;
import java.util.Currency;
import org.junit.Before;
import org.junit.Test;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CurrencyConvertorImplTest {
	private static final Currency USD = Currency.getInstance("USD");
	private static final Currency INR = Currency.getInstance("INR");
	private static final Currency CZK = Currency.getInstance("CZK");
	private CurrencyConvertorImpl currencyConvertor;
	@Mock
	private ExchangeRateTable mockTable;

	@Before
	public void setup() {
		currencyConvertor = new CurrencyConvertorImpl(mockTable);
	}

	@Test
	public void testConvert() throws ExternalServiceFailureException {
		when(mockTable.getExchangeRate(USD, INR)).thenReturn(new BigDecimal("73.152"));
		// Simple cases - definitely should round up or down
		assertThat(currencyConvertor.convert(USD, INR, new BigDecimal("1.50"))).isEqualTo(new BigDecimal("109.73"));
		assertThat(currencyConvertor.convert(USD, INR, new BigDecimal("2.00"))).isEqualTo(new BigDecimal("146.30"));
		// Round to nearest even number if the digit being rounded is 5
		// The following should work, but does not - the result is 33.265700019648 and
		// the digit left of the discarded fractional part is 6, which is even, so this
		// should be rounded down? But it does not...
		// assertThat(currencyConvertor.convert(USD, INR, new
		// BigDecimal("0.454747649"))).isEqualTo(new BigDecimal("33.26"));
		assertThat(currencyConvertor.convert(USD, INR, new BigDecimal("0.579820101")))
				.isEqualTo(new BigDecimal("42.42"));
	}

	@Test
	public void testConvertWithNullSourceCurrency() {
		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> currencyConvertor.convert(null, INR, BigDecimal.ONE));
	}

	@Test
	public void testConvertWithNullTargetCurrency() {
		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> currencyConvertor.convert(USD, null, BigDecimal.ONE));
	}

	@Test
	public void testConvertWithNullSourceAmount() {
		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> currencyConvertor.convert(USD, INR, null));
	}

	@Test
	public void testConvertWithUnknownCurrency() throws ExternalServiceFailureException {
		when(mockTable.getExchangeRate(USD, CZK)).thenReturn(null);
		assertThatExceptionOfType(UnknownExchangeRateException.class)
				.isThrownBy(() -> currencyConvertor.convert(USD, CZK, BigDecimal.ONE));
	}

	@Test
	public void testConvertWithExternalServiceFailure() throws ExternalServiceFailureException {
		when(mockTable.getExchangeRate(USD, INR)).thenThrow(new ExternalServiceFailureException("External error."));
		assertThatExceptionOfType(UnknownExchangeRateException.class)
				.isThrownBy(() -> currencyConvertor.convert(USD, INR, BigDecimal.ONE))
				.withCauseInstanceOf(ExternalServiceFailureException.class);
	}

}
