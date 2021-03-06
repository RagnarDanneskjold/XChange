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
package com.xeiam.xchange.vircurex;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joda.money.BigMoney;
import org.joda.money.CurrencyUnit;
import org.joda.money.IllegalCurrencyException;

import com.xeiam.xchange.currency.MoneyUtils;
import com.xeiam.xchange.dto.Order.OrderType;
import com.xeiam.xchange.dto.account.AccountInfo;
import com.xeiam.xchange.dto.marketdata.Ticker;
import com.xeiam.xchange.dto.trade.LimitOrder;
import com.xeiam.xchange.dto.trade.Wallet;
import com.xeiam.xchange.vircurex.dto.marketdata.VircurexAccountInfoReturn;

/**
 * Various adapters for converting from BTCE DTOs to XChange DTOs
 */
public final class VircurexAdapters {

	// private static final Logger log =
	// LoggerFactory.getLogger(VircurexAdapters.class);

	/**
	 * private Constructor
	 */
	private VircurexAdapters() {

	}

	public static LimitOrder adaptOrder(BigDecimal amount, BigDecimal price, String tradableIdentifier, String currency, String orderTypeString, String id) {

		// place a limit order
		OrderType orderType = orderTypeString.equalsIgnoreCase("bid") ? OrderType.BID : OrderType.ASK;
		BigMoney limitPrice;
		limitPrice = MoneyUtils.parse(currency + " " + price);

		return new LimitOrder(orderType, amount, tradableIdentifier, currency, limitPrice);

	}

	public static List<LimitOrder> adaptOrders(List<BigDecimal[]> someOrders, String tradableIdentifier, String currency, String orderType, String id) {

		List<LimitOrder> limitOrders = new ArrayList<LimitOrder>();

		// Bid orderbook is reversed order. Insert at index 0 instead of
		for (BigDecimal[] order : someOrders) {
			// appending
			if (orderType.equalsIgnoreCase("bid")) {
				limitOrders.add(0, adaptOrder(order[1], order[0], tradableIdentifier, currency, orderType, id));
			} else {
				limitOrders.add(adaptOrder(order[1], order[0], tradableIdentifier, currency, orderType, id));
			}
		}

		return limitOrders;
	}

	public static Ticker adaptTicker(Object bTCETicker, String tradableIdentifier, String currency) {
		throw new RuntimeException("Not supported.");
	}

	public static AccountInfo adaptAccountInfo(VircurexAccountInfoReturn vircurexAccountInfo) {
		List<Wallet> wallets = new ArrayList<Wallet>();
		Map<String, Map<String, BigDecimal>> funds = vircurexAccountInfo.getAvailableFunds();

		for (String lcCurrency : funds.keySet()) {
			String currency = lcCurrency.toUpperCase();
			try {
				CurrencyUnit.of(currency);
			} catch (IllegalCurrencyException e) {
				// System.out.println("Ignoring unknown currency " + currency);
				continue;
			}
			wallets.add(Wallet.createInstance(currency, funds.get(lcCurrency).get("availablebalance")));
		}
		return new AccountInfo(vircurexAccountInfo.getAccount(), wallets);
	}
}
