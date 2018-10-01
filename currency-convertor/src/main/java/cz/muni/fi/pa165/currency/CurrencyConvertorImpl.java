package cz.muni.fi.pa165.currency;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Currency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This is base implementation of {@link CurrencyConvertor}.
 *
 * @author petr.adamek@embedit.cz
 */
public class CurrencyConvertorImpl implements CurrencyConvertor {

    private final ExchangeRateTable exchangeRateTable;
    private final Logger logger = LoggerFactory.getLogger(CurrencyConvertorImpl.class);

    public CurrencyConvertorImpl(ExchangeRateTable exchangeRateTable) {
        this.exchangeRateTable = exchangeRateTable;
    }

    @Override
    public BigDecimal convert(Currency sourceCurrency, Currency targetCurrency, BigDecimal sourceAmount) {
logger.trace("Converting {} {} to {}", sourceAmount, sourceCurrency, targetCurrency);
    	if(sourceCurrency == null) {
	throw new IllegalArgumentException("Source currency can not be null.");
}
if(targetCurrency == null) {
	throw new IllegalArgumentException("Target currency can not be null.");
}
if(sourceAmount == null) {
	throw new IllegalArgumentException("Source amount can not be null.");
}
try {
	BigDecimal rate = exchangeRateTable.getExchangeRate(sourceCurrency, targetCurrency);
	if(rate == null) {
		logger.warn("No exchange data for conversion from {} to {}.", sourceCurrency, targetCurrency);
		throw new UnknownExchangeRateException("No exchange rate for the source and target currencies");
	}
		return sourceAmount.multiply(rate).setScale(2, RoundingMode.HALF_EVEN);
}
catch(ExternalServiceFailureException ex) {
logger.error("External service failed during conversion from {} to {}.", sourceCurrency, targetCurrency, ex);
	throw new UnknownExchangeRateException("External service error", ex);
}
    }

}
