/**
 * Copyright (C) 2012 - 2013 Xeiam LLC http://xeiam.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.xeiam.xchange.bitstamp.service.polling;

import java.io.IOException;
import java.math.BigDecimal;

import si.mazi.rescu.ParamsDigest;
import si.mazi.rescu.RestProxyFactory;

import com.xeiam.xchange.ExchangeException;
import com.xeiam.xchange.ExchangeSpecification;
import com.xeiam.xchange.bitstamp.BitstampAdapters;
import com.xeiam.xchange.bitstamp.BitstampAuthenticated;
import com.xeiam.xchange.bitstamp.BitstampUtils;
import com.xeiam.xchange.bitstamp.dto.account.BitstampBalance;
import com.xeiam.xchange.bitstamp.service.BitstampDigest;
import com.xeiam.xchange.dto.account.AccountInfo;
import com.xeiam.xchange.service.polling.BasePollingExchangeService;
import com.xeiam.xchange.service.polling.PollingAccountService;

/**
 * @author Matija Mazi
 */
public class BitstampPollingAccountService extends BasePollingExchangeService implements PollingAccountService {
  private BitstampAuthenticated bitstampAuthenticated;
  private ParamsDigest signatureCreator;

	private static AccountInfo accountInfo;
	private static long lastCache = 0;

	/**
	 * Constructor
	 * 
	 * @param exchangeSpecification
	 *            The {@link ExchangeSpecification}
	 */
	public BitstampPollingAccountService(ExchangeSpecification exchangeSpecification) {
    super(exchangeSpecification);
    this.bitstampAuthenticated = RestProxyFactory.createProxy(BitstampAuthenticated.class, exchangeSpecification.getSslUri());
    signatureCreator = BitstampDigest.createInstance(exchangeSpecification.getSecretKey(), exchangeSpecification.getUserName(), exchangeSpecification.getApiKey());
  }
	@Override
	public AccountInfo getAccountInfo() {
    BitstampBalance bitstampBalance = bitstampAuthenticated.getBalance(exchangeSpecification.getApiKey(), signatureCreator, BitstampUtils.getNonce());
    if (bitstampBalance.getError() != null) {
      throw new ExchangeException("Error getting balance. " + bitstampBalance.getError());
    }

		return accountInfo = BitstampAdapters.adaptAccountInfo(bitstampBalance, exchangeSpecification.getUserName());
	}
  @Override
  public String withdrawFunds(BigDecimal amount, String address) throws IOException {
    return bitstampAuthenticated.withdrawBitcoin(exchangeSpecification.getApiKey(), signatureCreator, BitstampUtils.getNonce(), amount, address).toString();
  }
  /**
   * This returns the currently set deposit address. It will not generate a new address (ie. repeated calls will return the same address).
   */
  @Override
  public String requestBitcoinDepositAddress(final String... arguments) throws IOException {
    return bitstampAuthenticated.getBitcoinDepositAddress(exchangeSpecification.getApiKey(), signatureCreator, BitstampUtils.getNonce());
  }

}
