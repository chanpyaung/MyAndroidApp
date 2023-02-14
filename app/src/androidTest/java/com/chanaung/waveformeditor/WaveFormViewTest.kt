package com.chanaung.waveformeditor

import android.view.View
import androidx.test.espresso.Espresso
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.chanaung.waveformeditor.views.WaveFormView
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WaveFormViewTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    private val waveforms = listOf(
        Pair(-0.05807495f, 0.037902832f),
        Pair(-0.15789795f, 0.14797974f),
        Pair(-0.41952515f, 0.65631104f),
        Pair(-0.5923767f, 0.78240967f),
        Pair(-0.3600464f, 0.55496216f),
        Pair(-0.3465271f, 0.53848267f),
        Pair(-0.25769043f, 0.30825806f),
    )

    @Test
    fun testWaveFromView() {
        val scenario = activityRule.scenario

        Espresso.onView(ViewMatchers.withId(R.id.waveFormView))
            .check(
                ViewAssertions.matches(
                   ViewMatchers.isDisplayed()
                )
            )
            .perform(performAddWaveForms(waveforms))
            .check(
                ViewAssertions.matches(
                    withSomeWaveForms(waveforms, false)
                )
            )
    }

    @Test
    fun testTrimmedWaveForms() {
        val start = 1
        val end = waveforms.size - 2
        val trimmedWaveForms = waveforms.slice(start..end)
        Espresso.onView(ViewMatchers.withId(R.id.waveFormView))
            .check(
                ViewAssertions.matches(
                    ViewMatchers.isDisplayed()
                )
            )
            .perform(performAddWaveForms(waveforms))
            .perform(performTrimmedWaveForms(start, end))
            .check(
                ViewAssertions.matches(
                    withSomeWaveForms(trimmedWaveForms, true)
                )
            )
    }

    private fun performAddWaveForms(waveForms: List<Pair<Float, Float>>): ViewAction {
        return object : ViewAction {
            override fun getDescription(): String = "action to add some waveForms"

            override fun getConstraints(): Matcher<View> = ViewMatchers.isAssignableFrom(WaveFormView::class.java)

            override fun perform(uiController: UiController?, view: View?) {
                val waveFormView = view as WaveFormView
                waveFormView.addWaveForms(waveForms)
            }

        }
    }

    private fun performTrimmedWaveForms(start: Int, end: Int): ViewAction {
        return object : ViewAction {
            override fun getDescription(): String = "action to trimmed waveform which should be same as original as the trimmer are not moved"

            override fun getConstraints(): Matcher<View> = ViewMatchers.isAssignableFrom(WaveFormView::class.java)

            override fun perform(uiController: UiController?, view: View?) {
                val waveFormView = view as WaveFormView
                waveFormView.addWaveForms(waveforms)
                waveFormView.trimAt(start, end)
//                waveFormView.getTrimmedWaveForm()
            }

        }
    }

    private fun withSomeWaveForms(expected: List<Pair<Float, Float>>, trimmed: Boolean): Matcher<View> {
        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description?) {
                description?.appendText("WaveFormView should have expected values")
            }

            override fun matchesSafely(item: View?): Boolean {
                if (item !is WaveFormView) {
                    return false
                }
                return if (trimmed)
                    item.getTrimmedWaveForm()?.first()?.first == expected.first().first && item.getTrimmedWaveForm()?.last()?.second == expected.last().second
                else item.getWaveForms()?.first() == expected.first()
            }
        }
    }
}