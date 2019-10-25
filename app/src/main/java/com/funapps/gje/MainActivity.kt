package com.funapps.gje

import android.app.PendingIntent.getActivity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener {
    companion object {
        const val TAG = "MainActivity"
    }

    private var numBtn: Int = 0
    private var numQuiz: Int = 0
    private var countTap: Int = 1
    private var isStartGame: Boolean = false
    private val listNum: List<Int> = Arrays.asList(
        R.drawable.num1,
        R.drawable.num2,
        R.drawable.num3,
        R.drawable.num4,
        R.drawable.num5,
        R.drawable.num6,
        R.drawable.num7,
        R.drawable.num8,
        R.drawable.num9
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setClickListenersOnViews()
    }

    override fun onClick(p0: View) {
        when (p0.id) {
            R.id.ivNum1 -> checkNum(1, ivNum1)
            R.id.ivNum2 -> checkNum(2, ivNum2)
            R.id.ivNum3 -> checkNum(3, ivNum3)
            R.id.ivNum4 -> checkNum(4, ivNum4)
            R.id.ivNum5 -> checkNum(5, ivNum5)
            R.id.ivNum6 -> checkNum(6, ivNum6)
            R.id.ivNum7 -> checkNum(7, ivNum7)
            R.id.ivNum8 -> checkNum(8, ivNum8)
            R.id.ivNum9 -> checkNum(9, ivNum9)
            R.id.mainBtn -> {
                countTap = 0
                numBtn = 0
                isStartGame = true
                mainBtn.setOnClickListener(null)
                mainBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.cover))
                numQuiz = (1..9).random()
            }
            R.id.ivAgain -> resetViewGame()
        }
    }

    private fun checkNum(n: Int, view: View) {
        if (isStartGame) {
            countTap++
            if (n == numQuiz) {
                youWin()
            } else {
                ivLessMore.visibility = View.VISIBLE
                view.alpha = 0.0f
                view.setOnClickListener(null)
                Log.d(TAG, "Count: " + countTap)
                if (n > numQuiz) {
                    ivLessMore.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.less))
                } else {
                    ivLessMore.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.more))
                }
            }
        }
    }

    private fun youWin() {
        mainBtn.setImageDrawable(ContextCompat.getDrawable(this, listNum[numQuiz - 1]))
        isStartGame = false
        ivWin.visibility = View.VISIBLE
        llTry.visibility = View.VISIBLE
        llScore.visibility = View.VISIBLE
        ivAgain.visibility = View.VISIBLE
        llNumberButtons.visibility = View.GONE
        ivLessMore.visibility = View.GONE
        when (countTap){
            1 -> ivTryWord.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.try3))
            in 2..4 -> ivTryWord.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.try2))
            in 5..9 -> ivTryWord.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.try1))
        }
        ivTryCount.setImageDrawable(ContextCompat.getDrawable(this, listNum[countTap-1]))
        ivScoreFirstNum.setImageDrawable(ContextCompat.getDrawable(this, listNum[9 - countTap]))
        Log.d(TAG, "Count win: " + countTap)
    }

    private fun resetViewGame() {
        isStartGame = true
        countTap = 0
        numBtn = 0
        numQuiz = (1..9).random()
        ivWin.visibility = View.GONE
        llTry.visibility = View.GONE
        llScore.visibility = View.GONE
        ivAgain.visibility = View.GONE
        llNumberButtons.visibility = View.VISIBLE
        ivNum1.alpha = 1.0f
        ivNum2.alpha = 1.0f
        ivNum3.alpha = 1.0f
        ivNum4.alpha = 1.0f
        ivNum5.alpha = 1.0f
        ivNum6.alpha = 1.0f
        ivNum7.alpha = 1.0f
        ivNum8.alpha = 1.0f
        ivNum9.alpha = 1.0f
        mainBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.cover))
        setClickListenersOnViews()
    }

    private fun setClickListenersOnViews() {
        mainBtn.setOnClickListener(this)
        ivNum1.setOnClickListener(this)
        ivNum2.setOnClickListener(this)
        ivNum3.setOnClickListener(this)
        ivNum4.setOnClickListener(this)
        ivNum5.setOnClickListener(this)
        ivNum6.setOnClickListener(this)
        ivNum7.setOnClickListener(this)
        ivNum8.setOnClickListener(this)
        ivNum9.setOnClickListener(this)
        ivAgain.setOnClickListener(this)
    }
}
