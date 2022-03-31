package edu.singaporetech.btco

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import edu.singaporetech.btco.databinding.ActivityLayoutBinding
import kotlinx.coroutines.*
import java.util.*

class BTCOActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    private lateinit var binding:ActivityLayoutBinding
    private external fun logDifficultyMsgNative(difficulty: String, message: String)
    private external fun mineGenesisBlockNative(difficulty: String)

    /**
     * Init everything needed when created.
     * - set button listeners
     * @param savedInstanceState the usual bundle of joy
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.genesisButton.setOnClickListener {
            if(!isValid(GENESIS)) return@setOnClickListener
            mineGenesisBlockNative(binding.difficultyEditText.text.toString())
        }

        binding.chainButton.setOnClickListener {
            if(!isValid(CHAIN)) return@setOnClickListener

            logDifficultyMsgNative(binding.difficultyEditText.text.toString(),
                binding.msgEditText.text.toString())
        }
    }

    private fun isValid(button: String): Boolean {
        binding.logTextView.text = ""
        val block = binding.blocksEditText.text
        val difficulty = binding.difficultyEditText.text
        val msg = binding.msgEditText.text
        var isValid = true

        when(button) {
            GENESIS -> {
                if (difficulty.isEmpty()) {
                    binding.logTextView.append(getString(R.string.difficult_cannot_be_empty))
                    isValid = false
                }

                if (!isWithinRange(difficulty.toString(), 1 , 10)) {
                    binding.logTextView.append("\n" + getString(R.string.difficulty_must_be_1_to_10))
                    isValid = false
                }

            }
            CHAIN -> {
                if (msg.isEmpty()) {
                    binding.logTextView.append(getString(R.string.describe_your_transaction_in_words))
                    isValid = false
                }

                if (difficulty.isEmpty()) {
                    binding.logTextView.append("\n" + getString(R.string.difficult_cannot_be_empty))
                    isValid = false
                }

                if (block.isEmpty()) {
                    binding.logTextView.append("\n" + getString(R.string.blocks_cannot_be_empty))
                    isValid = false
                }

                if (!isWithinRange(block.toString(), 2, 888)) {
                    binding.logTextView.append("\n" + getString(R.string.blocks_must_be_2_to_888))
                    isValid = false
                }
            }
        }
        return isValid
    }

    private fun isWithinRange(str: String, min: Int, max: Int): Boolean {
        if (str.isEmpty()) return false
        return str.toInt() in min..max
    }

    companion object {
        private val TAG = BTCOActivity::class.java.simpleName
        private const val GENESIS = "Genesis"
        private const val CHAIN = "Chain"

        init {
            System.loadLibrary("btco")
        }
    }

}