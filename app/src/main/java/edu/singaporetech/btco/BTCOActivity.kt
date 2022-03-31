package edu.singaporetech.btco

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import edu.singaporetech.btco.databinding.ActivityLayoutBinding
import kotlinx.coroutines.*

class BTCOActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    private lateinit var binding:ActivityLayoutBinding
    private lateinit var difficulty: String
    private lateinit var blocks: String
    private lateinit var message: String

    private external fun logDifficultyMsgNative(difficulty: Int, message: String)
    private external fun mineGenesisBlockNative(difficulty: Int): String
    private external fun mineBlocksNative(blocks: Int, difficulty: Int, message: String): String


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
            runGenesis()
        }

        binding.chainButton.setOnClickListener {
            getInputs()
            runChain()
        }
    }

    /**
     * 1. Check if inputs are valid
     * 2. start timer
     * 3. call native function to mine genesis block
     * 4. calculate time taken
     * 5. display result
     */
    private fun runGenesis() {
        if(!isValid(GENESIS)) return

        launch {
            val start = System.currentTimeMillis()

            var hash: String
            withContext(Dispatchers.Default) {
                hash = mineGenesisBlockNative(difficulty.toInt())
            }

            val time = System.currentTimeMillis() - start

            binding.dataHashTextView.text = hash
            binding.logTextView.text = getString(R.string.time_taken_to_mine, time.toString())
        }
    }

    /**
     * 1. Check if inputs are valid
     * 2. start timer
     * 3. call native function to mine blocks
     * 4. calculate time taken
     * 5. display result
     */
    private fun runChain() {
        if(!isValid(CHAIN)) return

        logDifficultyMsgNative(difficulty.toInt(), message)

        launch {
            val start = System.currentTimeMillis()

            var hash: String
            withContext(Dispatchers.Default) {
                hash = mineBlocksNative(blocks.toInt(), difficulty.toInt(), message)
            }

            val time = System.currentTimeMillis() - start

            binding.dataHashTextView.text = hash
            binding.logTextView.text = getString(R.string.time_taken_to_mine, time.toString())
        }
    }

    /**
     * Get input for blocks, difficulty and message.
     */
    private fun getInputs() {
        blocks = binding.blocksEditText.text.toString()
        difficulty = binding.difficultyEditText.text.toString()
        message = binding.msgEditText.text.toString()
    }

    /**
     * Check if inputs are valid and show error if not.
     *
     * Genesis:
     *  - difficulty must not be empty
     *  - difficulty within 1 to 10
     *
     *  Chain:
     *  - difficulty must not be empty
     *  - difficulty within 1 to 10
     *  - blocks must not be empty
     *  - blocks within 2 to 888
     *
     *
     * @param button the button that was clicked
     * @return true if valid, false if not
     */
    private fun isValid(button: String): Boolean {
        binding.logTextView.text = ""
        var isValid = true

        if (difficulty.isEmpty()) {
            binding.logTextView.append(getString(R.string.difficult_cannot_be_empty))
            isValid = false
        }

        if (!isWithinRange(difficulty, 1 , 10)) {
            binding.logTextView.append("\n" + getString(R.string.difficulty_must_be_1_to_10))
            isValid = false
        }

        if (button == CHAIN) {
            if (message.isEmpty()) {
                binding.logTextView.append("\n" + getString(R.string.describe_your_transaction_in_words))
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

        return isValid
    }

    /**
     * Check if a string is within a range.
     *
     * @param str the string to check
     * @param min the minimum value
     * @param max the maximum value
     *
     * @return true if within range, false if not
     */
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