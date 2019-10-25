package brs.transaction.duplicates

import brs.entity.Transaction

class TransactionDuplicationResult(val isDuplicate: Boolean, val transaction: Transaction?)
