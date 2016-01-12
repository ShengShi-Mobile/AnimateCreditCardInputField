package com.example.animatecreditcardinputfield;

import java.util.regex.Pattern;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;


public class CardEditView extends LinearLayout {

	private Animation detailsInAnim;
	private Animation detailsOutAnim;
	private Animation numberInAnim;
	private Animation numberOutAnim;

	private ImageView cardBrand;
	private EditText cardNumberField;
	private TextView lastFourDigitsField;
	private EditText expirationDateField;
	private EditText postalField;
	private ViewFlipper viewFlipper;
	private String cardType;

	static private String[] CREDIT_CARD_TYPES = new String[] { "DEFAULT", "VISA", "MASTER",
			"DISCOVER" };

	final Pattern CODE_PATTERN = Pattern
			.compile("([0-9]{0,4})|([0-9]{4} )+|([0-9]{4} [0-9]{0,4})+");

	public CardEditView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init(context, null);
	}

	public CardEditView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attributes) {
		LayoutInflater.from(context).inflate(R.layout.card_edit_view, this, true);

		detailsInAnim = AnimationUtils.loadAnimation(context, R.anim.card_editor_details_in);
		detailsOutAnim = AnimationUtils.loadAnimation(context, R.anim.card_editor_details_out);
		numberInAnim = AnimationUtils.loadAnimation(context, R.anim.card_editor_number_in);
		numberOutAnim = AnimationUtils.loadAnimation(context, R.anim.card_editor_number_out);

		// setGravity(12);

		cardBrand = (ImageView) findViewById(R.id.ce_card_brand);

		viewFlipper = (ViewFlipper) findViewById(R.id.ce_view_flipper);

		cardNumberField = (EditText) findViewById(R.id.ce_card_number_field);
		cardNumberField.setInputType(InputType.TYPE_CLASS_NUMBER);
		cardNumberField.addTextChangedListener(creditcardWatcher);

		lastFourDigitsField = (TextView) findViewById(R.id.ce_last_four_digits_field);
		lastFourDigitsField.setOnClickListener(NumberLabelListener);

		expirationDateField = (EditText) findViewById(R.id.ce_expiration_date_field);
		expirationDateField.setInputType(InputType.TYPE_CLASS_NUMBER);
		expirationDateField.addTextChangedListener(expirationDateWatcher);

		postalField = (EditText) findViewById(R.id.ce_postal_field);
		postalField.setInputType(InputType.TYPE_CLASS_NUMBER);

	}

	// ---------------Animation-----------------//
	private void flipTo(int i, Animation animation1, Animation animation2) {

		if (viewFlipper.getDisplayedChild() != i) {
			viewFlipper.setInAnimation(animation1);
			viewFlipper.setOutAnimation(animation2);
			viewFlipper.setDisplayedChild(i);
		}
	}

	private void showCardNumberPanel() {
		flipTo(0, numberInAnim, detailsOutAnim);
		cardNumberField.requestFocus();

	}

	private void showDetailPanel() {
		flipTo(1, detailsInAnim, numberOutAnim);
		expirationDateField.requestFocus();
	}

	// ---------------------------Credit Card Type and Valid
	// Check---------------------------------//
	private TextWatcher creditcardWatcher = new TextWatcher() {
		private CharSequence temp;
		private int selectionStart;
		private int selectionEnd;

		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub
			selectionStart = cardNumberField.getSelectionStart();

			selectionEnd = cardNumberField.getSelectionEnd();
			try {

				if (temp.length() > 19) {
					if (selectionStart > 0) {
						s.delete(selectionStart - 1, selectionEnd);
					}
					int tempSelection = selectionStart;
					cardNumberField.setText(s);
					cardNumberField.setSelection(tempSelection);
				} else {
					if (s.length() > 0 && !CODE_PATTERN.matcher(s).matches()) {
						String input = s.toString();
						String numbersOnly = keepNumbersOnly(input);
						String code = formatNumbersAsCode(numbersOnly);
						cardNumberField.removeTextChangedListener(this);
						cardNumberField.setText(code);

						// Remember the previous position of the cursor
						cardNumberField.setSelection(code.length());
						cardNumberField.addTextChangedListener(this);
					}
				}
			}// try
			catch (NumberFormatException e) {
			}// catch

		}

		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			// TODO Auto-generated method stub
			temp = s;
		}

		public void onTextChanged(CharSequence s, int start, int before, int count) {
			// TODO Auto-generated method stub
			checkCreditCardType(keepNumbersOnly(cardNumberField.getText().toString()));
		}

	};

	private String keepNumbersOnly(CharSequence s) {
		return s.toString().replaceAll("[^0-9]", ""); // Should of course be
														// more robust
	}

	private String formatNumbersAsCode(CharSequence s) {
		int groupDigits = 0;
		String tmp = "";
		for (int i = 0; i < s.length(); ++i) {
			tmp += s.charAt(i);
			++groupDigits;
			if (groupDigits == 4) {
				tmp += " ";
				groupDigits = 0;
			}
		}
		return tmp;
	}

	/**
	 * Checks credit card type based on the number
	 * 
	 * @param ccNum
	 *            the credit card number
	 * @return
	 */
	private void checkCreditCardType(String ccNum) throws NumberFormatException {
		cardType = getCardType(ccNum);
		setCardImage();
		if (!cardType.equalsIgnoreCase(CREDIT_CARD_TYPES[0])) {
			if ((ccNum.length() == 16) && (ccNum.startsWith("X") || validateCardNumber(ccNum))) {
				lastFourDigitsField.setText(getLastFourDigits(keepNumbersOnly(cardNumberField
						.getText().toString())));
				showDetailPanel();
			}
		}
	}

	/**
	 * Validates the credit card number using the Luhn algorithm
	 * 
	 * @param cardNumber
	 *            the credit card number
	 * @return
	 */
	public boolean validateCardNumber(String cardNumber) throws NumberFormatException {
		int sum = 0, digit, addend = 0;
		boolean doubled = false;
		for (int i = cardNumber.length() - 1; i >= 0; i--) {
			digit = Integer.parseInt(cardNumber.substring(i, i + 1));
			if (doubled) {
				addend = digit * 2;
				if (addend > 9) {
					addend -= 9;
				}
			} else {
				addend = digit;
			}
			sum += addend;
			doubled = !doubled;
		}
		return (sum % 10) == 0;
	}

	// --------------------------Number label
	// part----------------------------------//
	private OnClickListener NumberLabelListener = new OnClickListener() {
		public void onClick(View v) {
			// TODO Auto-generated method stub
			showCardNumberPanel();
		}
	};

	private CharSequence getLastFourDigits(String Num) {
		CharSequence digits;
		digits = Num.subSequence(Num.length() - 4, Num.length());
		return digits;
	}

	// ----------------------------Expiration Date
	// Field-----------------------------//
	private TextWatcher expirationDateWatcher = new TextWatcher() {
		private CharSequence editedText;

		public void afterTextChanged(Editable e) {
			// TODO Auto-generated method stub
			try {
				String output = "";
				String[] monthYear = editedText.toString().split("/");
				if (monthYear != null && monthYear.length > 0) {
					String monthString = monthYear[0];
					int monthInt = Integer.parseInt(monthString);

					if (monthInt > 12) {
						monthString = monthString.substring(0, 1);
					} else if (monthInt > 1 && monthInt <= 9) {
						monthString = "0" + monthInt;
					} else {
						monthString = monthInt + "";
					}
					if (monthString.length() == 2) {
						output = monthString + "/";
					} else {
						output = monthString;
					}
				}
				if (monthYear != null && monthYear.length > 1) {
					String yearString = monthYear[1];
					int yearInt = Integer.parseInt(yearString);

					if (yearString.length() == 1 && yearInt < 2) {
						// expiry should be in 2XXX series
					} else {
						output = output + yearString;
					}
				}
				expirationDateField.removeTextChangedListener(this);
				expirationDateField.setText(output);

				// // Remember the previous position of the cursor
				expirationDateField.setSelection(output.length());
				expirationDateField.addTextChangedListener(this);

			}// try
			catch (NumberFormatException exception) {
			}// catch
		}

		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			// TODO Auto-generated method stub
			editedText = s;
			if (after == 0) {
				if (editedText.charAt(editedText.length() - 1) == '/') {
					editedText = editedText.toString().substring(0, editedText.length() - 2);
				}
			}
		}

		public void onTextChanged(CharSequence s, int start, int before, int count) {
			// TODO Auto-generated method stub
			if (expirationDateField.getText().toString().length() == 7) {
				postalField.requestFocus();
			}
		}

	};

	// ------------------------------------------------------------------//
	public String getCardNumber() {
		return keepNumbersOnly(cardNumberField.getText().toString());
	}

	public View getCardNumberView() {
		return cardNumberField;
	}

	public View getCardExpiryView() {
		return expirationDateField;
	}

	// Srikanth: setCardType Before to Edit it properly.
	public void setCardNumber(String cardNum) {
		cardNumberField.setText(cardNum.toString().replaceAll("-", " "));
		// @ajimol added below to set card type image
		checkCreditCardType(cardNum);
	}

	public String getExpirationDate() {
		return expirationDateField.getText().toString();
	}

	// @ajimol removed
	public void setExpirationDate(String expirationDate) {
		expirationDateField.setText(expirationDate.toString());
	}

	// public String getCvv() {
	// return cvvField.getText().toString();
	// }
	//
	// public void setCvv(String cvv) {
	// cvvField.setText(cvv.toString());
	// }

	public String getPostalCode() {
		return postalField.getText().toString();
	}

	public void setPostalCode(String zip) {
		postalField.setText(zip.toString());
	}

	public String getCardType(String ccNum) {
		if (ccNum.length() >= 1 && ccNum.startsWith("4")) {
			cardType = CREDIT_CARD_TYPES[1];
		} else if ((ccNum.length() >= 2)
				&& (ccNum.startsWith("51") || ccNum.startsWith("52") || ccNum.startsWith("53")
						|| ccNum.startsWith("54") || ccNum.startsWith("55"))) {

			cardType = CREDIT_CARD_TYPES[2];
		} else if ((ccNum.length() >= 4)
				&& (ccNum.startsWith("6011") || ccNum.startsWith("65") || ccNum.startsWith("644")
						|| ccNum.startsWith("645") || ccNum.startsWith("646")
						|| ccNum.startsWith("647") || ccNum.startsWith("648") || ccNum
							.startsWith("649"))) {
			cardType = CREDIT_CARD_TYPES[3];
		} else if (ccNum.length() >= 4 && getCardType() != null && ccNum.startsWith("XXXX")) {
			// For Masked Card.
			cardType = getCardType();
			lastFourDigitsField.setText(getLastFourDigits(keepNumbersOnly(cardNumberField
					.getText().toString())));
			lastFourDigitsField.setEnabled(false);
		} else {
			cardType = CREDIT_CARD_TYPES[0];
		}
		return cardType;
	}

	public String getCardType() {
		return cardType;
	}

	public void setCardType(String cardType) {
		this.cardType = cardType;
		setCardImage();
		showDetailPanel();
	}

	private void setCardImage() {

		if (cardType.equalsIgnoreCase(CREDIT_CARD_TYPES[0])) {
			cardBrand.setImageResource(R.drawable.cc_default);
		} else if (cardType.equalsIgnoreCase(CREDIT_CARD_TYPES[1])) {
			cardBrand.setImageResource(R.drawable.cc_visa);
		} else if (cardType.equalsIgnoreCase(CREDIT_CARD_TYPES[2])) {
			cardBrand.setImageResource(R.drawable.cc_mastercard);
		} else if (cardType.equalsIgnoreCase(CREDIT_CARD_TYPES[3])) {
			cardBrand.setImageResource(R.drawable.cc_discover);
		}

	}

}
