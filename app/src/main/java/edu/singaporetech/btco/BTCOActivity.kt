package edu.singaporetech.btco

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import edu.singaporetech.btco.databinding.ActivityLayoutBinding
import kotlinx.coroutines.*
import java.util.*

class BTCOActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    private lateinit var binding:ActivityLayoutBinding
    private external fun logDifficultyMsgNative(difficulty: String, message: String)
    private external fun mineGenesisBlockNative(difficulty: Int): String
    private external fun mineBlocksNative(blocks: Int, difficulty: Int, message: String): String

    private lateinit var difficulty: String
    private lateinit var blocks: String
    private lateinit var message: String

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
            getInputs()
            if(!isValid(GENESIS)) return@setOnClickListener

            launch {
                var hash: String
                withContext(Dispatchers.Default) {
                    hash = mineGenesisBlockNative(difficulty.toInt())
                }
                binding.dataHashTextView.text = hash
            }
        }

        binding.chainButton.setOnClickListener {
            getInputs()
            if(!isValid(CHAIN)) return@setOnClickListener

            logDifficultyMsgNative(difficulty, message)
            launch {
                var hash: String
                withContext(Dispatchers.Default) {
                    hash = mineBlocksNative(blocks.toInt(), difficulty.toInt(), message)
                }
                binding.dataHashTextView.text = hash
            }
        }
    }

    private fun getInputs() {
        blocks = binding.blocksEditText.text.toString()
        difficulty = binding.difficultyEditText.text.toString()
        message = binding.msgEditText.text.toString()
    }

    private fun isValid(button: String): Boolean {
        binding.logTextView.text = ""

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
                if (message.isEmpty()) {
                    binding.logTextView.append(getString(R.string.describe_your_transaction_in_words))
                    isValid = false
                }

                if (difficulty.isEmpty()) {
                    binding.logTextView.append("\n" + getString(R.string.difficult_cannot_be_empty))
                    isValid = false
                }

                if (blocks.isEmpty()) {
                    binding.logTextView.append("\n" + getString(R.string.blocks_cannot_be_empty))
                    isValid = false
                }

                if (!isWithinRange(blocks, 2, 888)) {
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