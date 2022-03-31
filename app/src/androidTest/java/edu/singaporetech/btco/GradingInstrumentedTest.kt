package edu.singaporetech.btco

import android.util.Log
import android.view.View
import android.widget.NumberPicker
import android.widget.TextView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import org.hamcrest.Matchers.*
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import junit.framework.TestCase.fail
import kotlinx.coroutines.*
import org.hamcrest.Matcher
import org.hamcrest.core.AllOf
import org.junit.*
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/**
 * Created by mrboliao on 20/4/17.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING) // run in alphabet order of method name
@RunWith(AndroidJUnit4::class)
@LargeTest
class GradingInstrumentedTest {
    private lateinit var device: UiDevice

    // to call suspend functions
    private val testScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // decor view for testing toasts
    private lateinit var decorView: View

    @get:Rule
    var testLogger = TestLogger()

    @get:Rule
    var activityRule = activityScenarioRule<BTCOActivity>()

    @Before
    fun setupUiAutomator() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    @Before
    fun setupDecorView() {
        activityRule.scenario.onActivity {
            decorView = it.window.decorView
        }
    }

    @Test
    fun a1_onLaunch_containsPicker() {
        Log.i(TAG, """
            
            ### 1. Number picker exist with even values between 2 to 256
            - launch app
            - check picker element picker_length exists
            - pick ${PICKEDVAL1} then pick ${PICKEDVAL_LAST} and check displayed values
            """.trimIndent())

        onView(withId(R.id.picker_length)).check(matches(isDisplayed()))

        onView(withId(R.id.picker_length)).perform(setNumberPickerByDisplayedVal(PICKEDVAL1))
        onView(withPickedDisplayedValue(PICKEDVAL1)).check(matches(
                hasDescendant(withText(containsString(PICKEDVAL1.toString())))))

        onView(withId(R.id.picker_length)).perform(setNumberPickerByDisplayedVal(PICKEDVAL_LAST))
        onView(withPickedDisplayedValue(PICKEDVAL_LAST)).check(matches(
                hasDescendant(withText(containsString(PICKEDVAL_LAST.toString())))))
    }

    @Test
    fun a2_onLaunch_containsRequiredUI() {
        Log.i(TAG, """
            
            ### 2. Other UI elements all exist
            - check EditText elements et_iters, et_old_code, et_salt exist
            - check we can type stuff in these EditText elements
            - check TextView element tv_better_code exists
            - check button "$BUTTON_TEXT" exists
            """.trimIndent())

        onView(withId(R.id.et_iters)).check(matches(isDisplayed()))
        onView(withId(R.id.et_iters)).perform(
                typeText(JIBBERISH),
                closeSoftKeyboard()
        )

        onView(withId(R.id.msgEditText)).check(matches(isDisplayed()))
        onView(withId(R.id.msgEditText)).perform(
                typeText(JIBBERISH),
                closeSoftKeyboard()
        )

        onView(withId(R.id.et_salt)).check(matches(isDisplayed()))
        onView(withId(R.id.et_salt)).perform(
                typeText(JIBBERISH),
                closeSoftKeyboard()
        )

        onView(withId(R.id.dataHashTextView)).check(matches(isDisplayed()))

        onView(allOf(
                withClassName(endsWith("Button")),
                withText(containsString(BUTTON_TEXT)))
        ).check(matches(isDisplayed()))
    }

    @Test
    fun a3_onEmptyInputs_noOutputDisplayed() {
        Log.i(TAG, """
            
            ### 3. Correct error text in empty input fields.   
            - type "$ITERS1" in et_iters, "" in et_old_code, "$SALT1" in et_salt
            - click $BUTTON_TEXT
            - check that "$HASH1" doesn't exist
            - check has error text matching "$ERR_STR_OLD_CODE"
            """.trimIndent())

        // check button exists
        val button = onView(AllOf.allOf(withClassName(endsWith("Button")), withText(containsString(BUTTON_TEXT))))

        // type text in EditText and close soft keyboard (else may block button)
        onView(withId(R.id.et_iters)).perform(clearText(), typeText(ITERS1), closeSoftKeyboard())
        onView(withId(R.id.msgEditText)).perform(clearText(), closeSoftKeyboard())
        onView(withId(R.id.et_salt)).perform(clearText(), typeText(SALT1), closeSoftKeyboard())
        button.perform(click())
        onView(withText(HASH1)).check(doesNotExist())
//        onView(withText(containsString(ERR_STR_OLD_CODE))).check(matches(isDisplayed()))
        onView(withId(R.id.msgEditText)).check(matches(hasErrorText(containsString(ERR_STR_OLD_CODE))))
    }

    @Test
    fun a4_onValidInputs_correctHashDisplayed() {
        Log.i(TAG, """
            
            ### 4. Correct BETTER CODE displayed for valid inputs
            - input picker_length: "$PICKEDVAL1" et_iters: "$ITERS1" in et_old_code: "$CODE1" et_salt: "$SALT1"
            - swipe up+down to trigger OnValueChangedListener and click $BUTTON_TEXT
            - check tv_hash matches $HASH1
            - input picker_length: "$PICKEDVAL2" et_iters: "$ITERS2" in et_old_code: "$CODE2" et_salt: "$SALT2"
            - swipe up+down to trigger OnValueChangedListener and click $BUTTON_TEXT
            - check tv_hash matches $HASH2
            """.trimIndent())

        try {
            // check button exists
            val button = onView(allOf(
                    withClassName(endsWith("Button")),
                    withText(containsString(BUTTON_TEXT))))

            onView(withId(R.id.picker_length)).perform(setNumberPickerByDisplayedVal(PICKEDVAL1))
            onView(withId(R.id.picker_length)).perform(swipeUp()) // to trigger the onChangedValue
            onView(withId(R.id.picker_length)).perform(swipeDown())
            onView(withId(R.id.et_iters)).perform(clearText(), typeText(ITERS1), closeSoftKeyboard())
            onView(withId(R.id.msgEditText)).perform(clearText(), typeText(CODE1), closeSoftKeyboard())
            onView(withId(R.id.et_salt)).perform(clearText(), typeText(SALT1), closeSoftKeyboard())
            button.perform(click())
            onView(withId(R.id.dataHashTextView)).check(matches(withText(HASH1)))

            onView(withId(R.id.picker_length)).perform(setNumberPickerByDisplayedVal(PICKEDVAL2))
            onView(withId(R.id.picker_length)).perform(swipeUp())  // to trigger the onChangedValue
            onView(withId(R.id.picker_length)).perform(swipeDown())
            onView(withId(R.id.et_iters)).perform(clearText(), typeText(ITERS2), closeSoftKeyboard())
            onView(withId(R.id.msgEditText)).perform(clearText(), typeText(CODE2), closeSoftKeyboard())
            onView(withId(R.id.et_salt)).perform(clearText(), typeText(SALT2), closeSoftKeyboard())
            button.perform(click())
            onView(withId(R.id.dataHashTextView)).check(matches(withText(HASH2)))
        } catch (t: Throwable) {
            fail("Crashed due to exception: ${t.localizedMessage}")
        }
    }

    @Test
    fun a5_onValidDiffCodeLengths_correctHashDisplayed() {
        Log.i(TAG, """
            
            ### 5. Correct BETTER CODE displayed for valid inputs of different lengths
            - input picker_length: "$PICKEDVAL3" et_iters: "$ITERS3" in et_old_code: "$CODE3" et_salt: "$SALT3"
            - swipe up+down to trigger OnValueChangedListener and click $BUTTON_TEXT
            - check tv_hash matches $HASH3
            - change picker_length: "$PICKEDVAL4"
            - click up+down to trigger OnValueChangedListener and $BUTTON_TEXT
            - check tv_hash matches $HASH4
            - change picker_length: "$PICKEDVAL5"
            - click up+down to trigger OnValueChangedListener and $BUTTON_TEXT
            - check tv_hash matches $HASH5
            """.trimIndent())

        // check button exists
        val button = onView(allOf(
                withClassName(endsWith("Button")),
                withText(containsString(BUTTON_TEXT))))

        onView(withId(R.id.picker_length)).perform(setNumberPickerByDisplayedVal(PICKEDVAL3))
        onView(withId(R.id.picker_length)).perform(swipeUp()) // to trigger the onChangedValue
        onView(withId(R.id.picker_length)).perform(swipeDown())
        onView(withId(R.id.et_iters)).perform(clearText(), typeText(ITERS3), closeSoftKeyboard())
        onView(withId(R.id.msgEditText)).perform(clearText(), typeText(CODE3), closeSoftKeyboard())
        onView(withId(R.id.et_salt)).perform(clearText(), typeText(SALT3), closeSoftKeyboard())
        button.perform(click())
        onView(withId(R.id.dataHashTextView)).check(matches(withText(HASH3)))

        onView(withId(R.id.picker_length)).perform(setNumberPickerByDisplayedVal(PICKEDVAL4))
        onView(withId(R.id.picker_length)).perform(swipeUp())  // to trigger the onChangedValue
        onView(withId(R.id.picker_length)).perform(swipeDown())
        button.perform(click())
        onView(withId(R.id.dataHashTextView)).check(matches(withText(HASH4)))

        onView(withId(R.id.picker_length)).perform(setNumberPickerByDisplayedVal(PICKEDVAL5))
        onView(withId(R.id.picker_length)).perform(swipeUp())  // to trigger the onChangedValue
        onView(withId(R.id.picker_length)).perform(swipeDown())
        button.perform(click())
        onView(withId(R.id.dataHashTextView)).check(matches(withText(HASH5)))
    }

    @Test
    fun a6_onValidDiffIters_correctHashDisplayed() {
        Log.i(TAG, """
            
            ### 6. Correct BETTER CODE displayed for valid inputs of different lengths
            - input picker_length: "$PICKEDVAL6" et_iters: "$ITERS6" in et_old_code: "$CODE6" et_salt: "$SALT6"
            - swipe up+down to trigger OnValueChangedListener and click $BUTTON_TEXT
            - check tv_hash matches $HASH6
            - change et_iters: "$ITERS7"
            - click $BUTTON_TEXT
            - check tv_hash matches $HASH7
            - change et_iters: "$ITERS8"
            - click $BUTTON_TEXT
            - check tv_hash matches $HASH8
            """.trimIndent())

        // check button exists
        val button = onView(allOf(
                withClassName(endsWith("Button")),
                withText(containsString(BUTTON_TEXT))))

        onView(withId(R.id.picker_length)).perform(setNumberPickerByDisplayedVal(PICKEDVAL6))
        onView(withId(R.id.picker_length)).perform(swipeUp()) // to trigger the onChangedValue
        onView(withId(R.id.picker_length)).perform(swipeDown())
        onView(withId(R.id.et_iters)).perform(clearText(), typeText(ITERS6), closeSoftKeyboard())
        onView(withId(R.id.msgEditText)).perform(clearText(), typeText(CODE6), closeSoftKeyboard())
        onView(withId(R.id.et_salt)).perform(clearText(), typeText(SALT6), closeSoftKeyboard())
        button.perform(click())
        onView(withId(R.id.dataHashTextView)).check(matches(withText(HASH6)))

        onView(withId(R.id.et_iters)).perform(clearText(), typeText(ITERS7), closeSoftKeyboard())
        button.perform(click())
        onView(withId(R.id.dataHashTextView)).check(matches(withText(HASH7)))

        onView(withId(R.id.et_iters)).perform(clearText(), typeText(ITERS8), closeSoftKeyboard())
        button.perform(click())
        onView(withId(R.id.dataHashTextView)).check(matches(withText(HASH8)))
    }
    @Test
    fun a7_onValidInputs_noANR() {
        Log.i(TAG, """
            
            ### 7. UI no lag or ANR when click HASH ME with large ITERATIONs
            - input picker_length: "$PICKEDVAL2" et_iters: "$ITERS9" in et_old_code: "$CODE2" et_salt: "$SALT2"
            - swipe up+down to trigger OnValueChangedListener and click $BUTTON_TEXT
            - check tv_hash matches $HASH9
            - check still can type in et_old_code and et_salt
            """.trimIndent())
        
        val button = onView(allOf(
                withClassName(endsWith("Button")),
                withText(containsString(BUTTON_TEXT))))

        onView(withId(R.id.picker_length)).perform(setNumberPickerByDisplayedVal(PICKEDVAL2))
        onView(withId(R.id.picker_length)).perform(swipeUp()) // to trigger the onChangedValue
        onView(withId(R.id.picker_length)).perform(swipeDown())
        onView(withId(R.id.et_iters)).perform(clearText(), typeText(ITERS9), closeSoftKeyboard())
        onView(withId(R.id.msgEditText)).perform(clearText(), typeText(CODE2), closeSoftKeyboard())
        onView(withId(R.id.et_salt)).perform(clearText(), typeText(SALT2), closeSoftKeyboard())
        button.perform(click())
        
        // check still can type
        onView(withId(R.id.msgEditText)).perform(clearText(), typeText(CODE3), closeSoftKeyboard())
        onView(withId(R.id.et_salt)).perform(clearText(), typeText(SALT3), closeSoftKeyboard())
        
        // onView(withId(R.id.tv_better_code)).check(matches(withText(HASH9)))
    }

    @Test
    fun a8_onValidInputs_hasToastShowingTimeAfterClicked() {
        Log.i(TAG, """
            
            ### 8. Correct toast with time displayed after BETTER CODE generated.'
            - input picker_length: "$PICKEDVAL2" et_iters: "$ITERS9" in et_old_code: "$CODE2" et_salt: "$SALT2"
            - swipe up+down to trigger OnValueChangedListener and click $BUTTON_TEXT
            - check Toast with TOAST_SUBSTR and TOAST_SUBSTR exists
            """.trimIndent())

        // check button exists
        val button = onView(AllOf.allOf(withClassName(endsWith("Button")), withText(containsString(BUTTON_TEXT))))

        onView(withId(R.id.picker_length)).perform(setNumberPickerByDisplayedVal(PICKEDVAL2))
        onView(withId(R.id.picker_length)).perform(swipeUp())  // to trigger the onChangedValue
        onView(withId(R.id.picker_length)).perform(swipeDown())
        onView(withId(R.id.et_iters)).perform(clearText(), typeText(ITERS2), closeSoftKeyboard())
        onView(withId(R.id.msgEditText)).perform(clearText(), typeText(CODE2), closeSoftKeyboard())
        onView(withId(R.id.et_salt)).perform(clearText(), typeText(SALT2), closeSoftKeyboard())
        button.perform(click())

//        // check whether toast is shown
//        try {
//            onView(withText(containsString(TOAST_SUBSTR))).inRoot(RootwithDecorView(Corenot(activityRule.activity.window.decorView)))
//                    .check(matches(isDisplayed()))
//        } catch (e: NoMatchingViewException) {
//            onView(withText(containsString(TOAST_SUBSTR2))).inRoot(RootwithDecorView(Corenot(activityRule.activity.window.decorView)))
//                    .check(matches(isDisplayed()))
//        }

        // Check if toast is displayed
        onView(anyOf(
                withText(equalToIgnoringCase(TOAST_SUBSTR)),
                allOf(
                        withText(containsString(TOAST_SUBSTR)),
                        withText(containsString(TOAST_SUBSTR2))
                )
        ))
                .inRoot(RootMatchers.withDecorView(not(`is`(decorView))))
                .check(matches(isDisplayed()))
    }


//    @Test
//    public void onStart_ableToCallNativeFunction() {
//        Log.i(TAG,"\n### 6. Able to call native function to get hashes." +
//                "\n hashMe method can be called directly from SHActivity class with input " +
//                "\"" + ITERS3 + "\", \"" + MESSAGE3 + "\", \"" + SALT3 + "\" returns " + HASH3 + " as output");
//
//        final String methodName = "hashMe";
//        String errMsg = "gives correct output hash";
//        Class[] args;
//        Method method;
//
//        // test whether various shaMe(x,y) exists,
//        // - construct the method call at runtime using java reflection
//        try {
//            args = new Class[]{String.class, String.class, Long.TYPE};
//            method = activityRule.getActivity().getClass().getDeclaredMethod(methodName, args);
//            assertEquals("input " + MESSAGE1 + ", " + SALT1 + ", " + ITERS1 + errMsg, HASH1,
//                    method.invoke(activityRule.getActivity(), MESSAGE1, SALT1, Long.parseLong(ITERS1)));
//        } catch (NoSuchMethodException e) {
//            try {
//                args = new Class[]{String.class, String.class, Integer.TYPE};
//                method = activityRule.getActivity().getClass().getDeclaredMethod(methodName, args);
//                assertEquals("input " + MESSAGE1 + ", " + SALT1 + ", " + ITERS1 + errMsg, HASH1,
//                        method.invoke(activityRule.getActivity(), MESSAGE1, SALT1, Integer.parseInt(ITERS1)));
//            } catch (NoSuchMethodException e2) {
//                fail(methodName + "(String, String, long) or shaMe(String String, Integer) all do not exist");
//            } catch (Exception e2) {
//                fail(methodName + "(String, String, Integer) method invocation error" + e2.getMessage());
//            }
//        } catch (Exception e3) {
//            fail(methodName + "(String, String, long) method invocation error " + e3.getMessage());
//        }
//    }
//

//    @Test
//    fun a9_onStart_ableToCallNativeFunction() {
//        Log.i(TAG, """
//            ### 9. Correct BETTER CODE displayed for valid inputs
//            - input picker_length: "$PICKEDVAL1" et_iters: "$ITERS1" in et_old_code: "$CODE1" et_salt: "$SALT1"
//            - swipe up+down to trigger OnValueChangedListener and click $BUTTON_TEXT
//            - check returned String
//            """.trimIndent())
//
//        val length = 8L
//        activityRule.scenario.onActivity {
//            testScope.launch {
//                val str = it.makeCodeGreatAgain(CODE1, SALT1, ITERS1.toLong(), length)
//                assertTrue("is string", str is String)
//            }
//        }
//
//        // note did not try to match String as some may have passed the length differently
//    }

    /**
     * Get text from a TextView
     */
    inner class GetTextAction : ViewAction {
        private var text: CharSequence? = null
        override fun getConstraints(): Matcher<View> {
            return isAssignableFrom(TextView::class.java)
        }

        override fun getDescription(): String {
            return "get text"
        }

        override fun perform(uiController: UiController, view: View) {
            val textView = view as TextView
            text = textView.text
        }

        fun getText(): String? {
            return text.toString()
        }
    }

    /**
     * This will watch all tests in a rule and do post actions.
     * 1. Add test info to report str
     * 2. accumulate marks.
     */
    inner class TestLogger : TestWatcher() {
        override fun succeeded(description: Description) {
            super.succeeded(description)
            Log.i(TAG, "- test PASSED.  \n")
        }

        override fun failed(e: Throwable, description: Description) {
            super.failed(e, description)
            var errStr = ""
            e.message?.let {
                if (it.length > REPORT_ITEM_MAX_LENGTH)
                    errStr = it.split("\n").toTypedArray()[0] + "... (err msg too long)"
                else
                    errStr = it

                Log.i(TAG, "- FAILED Reason-> $errStr \n")
            }
        }
    }

    companion object {
        private const val TAG = "GradingInstrumentedTest"
        private const val TAG_INT = "GradingInstrumentedTest_INT"
        protected const val REPORT_ITEM_MAX_LENGTH = 350
        private const val JIBBERISH = "l33tc0d3"

        private const val TOAST_SUBSTR = "ook"
        private const val TOAST_SUBSTR2 = "ms"

        private const val PICKEDVAL_LAST = 256

        private const val PICKEDVAL1 = 2
        private const val ITERS1 = "1"
        private const val CODE1 = "11111111"
        private const val SALT1 = "a"
        private const val HASH1 = "EB"

        private const val PICKEDVAL2 = 68
        private const val ITERS2 = "8"
        private const val CODE2 = "eightish"
        private const val SALT2 = "height"
        private const val HASH2 = "EA9FB3738A24B29E28FF3231EAE5DBCCA5AC6AEF32C9EDE4A538C9AFC3458ACD015B"

        private const val PICKEDVAL3 = 8
        private const val ITERS3 = "88888"
        private const val CODE3 = "salt.ish"
        private const val SALT3 = "giam"
        private const val HASH3 = "1BEA033C"

        private const val PICKEDVAL4 = 10
        private const val HASH4 = "1BEA033C2D"

        private const val PICKEDVAL5 = 20
        private const val HASH5 = "1BEA033C2D47B24AB6AF"

        private const val PICKEDVAL6 = 20
        private const val ITERS6 = "111"
        private const val CODE6 = "salt.ish"
        private const val SALT6 = "giam"
        private const val HASH6 = "44C6C44F7AAB41E539FC"

        private const val ITERS7 = "1010"
        private const val HASH7 = "A437DC0696CB852C11A9"

        private const val ITERS8 = "1110"
        private const val HASH8 = "AF3FF7EBA308C6F812A9"

        private const val ITERS9 = "8888888"
        private const val HASH9 = "0690F4421762FDD4EB8F693E7756424D993ACDC8169E8CB5CFB60052CE7C3DB91335"

        private const val ERR_STR_OLD_CODE = "input code here"

        // test vars
        protected const val BUTTON_TEXT = "MAKE BETTER CODE"

        /**
         * Custom ViewMatcher to check if value in NumberPicker is equals to expectedNum
         */
        fun withPickedDisplayedValue(expectedDisplayedVal: Int): Matcher<View> {
            return object : BoundedMatcher<View, NumberPicker>(NumberPicker::class.java) {
                override fun matchesSafely(item: NumberPicker): Boolean {
                    return try {
                        var pickedDisplayedVal = -1
                        if (item.displayedValues != null) {
                            pickedDisplayedVal = item.displayedValues[item.value].toInt()
                            Log.i(TAG_INT, "in withPickedValue matcher displayedValues NOT NULL expectedDisplayedVal=$expectedDisplayedVal pickedDisplayedVal=$pickedDisplayedVal")
                        }
                        else {
                            pickedDisplayedVal = item.value * 2
                            Log.i(TAG_INT, "in withPickedValue matcher displayedValues NULL expectedDisplayedVal=$expectedDisplayedVal pickedDisplayedVal=$pickedDisplayedVal")
                        }
                        pickedDisplayedVal == expectedDisplayedVal
                    } catch (t: Throwable) {
                        Log.i(TAG_INT, "strange exception: ${t.localizedMessage}")
                        false
                    }
                }

                override fun describeTo(description: org.hamcrest.Description) {
                    description.appendText("the expected NumberPicker displayed value of $expectedDisplayedVal")
                }
            }
        }

        /**
         * Perform action of choosing number from NumberPicker
         */
        fun setNumberPicker(value: Int): ViewAction {
            return object : ViewAction {
                override fun perform(uiController: UiController, view: View) {
                    val picker = view as NumberPicker
                    picker.value = value
                    Log.i(TAG_INT, "PICKER set val=${picker.value} displayedValue=${picker.displayedValues[value]}")
                }

                override fun getDescription(): String {
                    return "set value in NumberPicker"
                }

                override fun getConstraints(): Matcher<View> {
                    return isAssignableFrom(NumberPicker::class.java)
                }
            }
        }

        /**
         * Perform action of choosing number from NumberPicker by the displayed val
         */
        fun setNumberPickerByDisplayedVal(displayedVal: Int): ViewAction {
            return object : ViewAction {
                override fun perform(uiController: UiController, view: View) {
                    val picker = view as NumberPicker

                    // find the right number based on the displayedVal
                    for (i in picker.minValue..picker.maxValue) {
                        picker.value = i

                        if (picker.displayedValues != null) {
                            Log.i(TAG_INT, "PICKER i=$i displayedValue=${picker.displayedValues[i]}")
                            if (displayedVal.toString() == picker.displayedValues[i])
                                break
                        }
                        else {
                            // assume that value is picker.value*2
                            Log.i(TAG_INT, "PICKER i=$i picker.value*2=${picker.value*2}")
                            if (displayedVal == (picker.value*2))
                                break
                        }
                    }

                    Log.i(TAG_INT, "PICKER set val=${picker.value}")
                }

                override fun getDescription(): String {
                    return "set value in NumberPicker"
                }

                override fun getConstraints(): Matcher<View> {
                    return isAssignableFrom(NumberPicker::class.java)
                }
            }
        }
    }
}