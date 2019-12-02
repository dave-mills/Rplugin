/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.r.editor.formatting

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.*
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.tree.ILazyParseableElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.util.containers.FactoryMap
import org.jetbrains.r.RLanguage
import org.jetbrains.r.parsing.RElementTypes
import org.jetbrains.r.parsing.RParserDefinition
import org.jetbrains.r.psi.api.*

private val NON_INDENT_PARTS = TokenSet.create(
  RElementTypes.R_RPAR,
  RElementTypes.R_RBRACE,
  RElementTypes.R_RBRACKET,
  RElementTypes.R_RDBRACKET,
  RElementTypes.R_ELSE
)

class RFormattingContext(private val settings: CodeStyleSettings) {
  private val spacingBuilder = createSpacingBuilder(settings)

  private val childIndentAlignments: MutableMap<ASTNode, Alignment> = FactoryMap.create { Alignment.createAlignment() }
  /** Use argument list as anchor */
  private val assignInParametersAlignments: MutableMap<ASTNode, Alignment> = FactoryMap.create { Alignment.createAlignment(true) }
  /** Use first element in row as anchor */
  private val alignmentByAnchor: MutableMap<ASTNode, Alignment> = FactoryMap.create { Alignment.createAlignment(true) }

  fun computeAlignment(node: ASTNode): Alignment? {
    val common = settings.getCommonSettings(RLanguage.INSTANCE)
    val custom = settings.getCustomSettings(RCodeStyleSettings::class.java)

    val nodeParent = node.treeParent ?: return null
    if ((common.ALIGN_MULTILINE_PARAMETERS_IN_CALLS &&
         nodeParent.elementType == RElementTypes.R_ARGUMENT_LIST &&
         (node.firstChildNode != null || isCommentAtEmptyLine(node))) ||
        (common.ALIGN_MULTILINE_PARAMETERS &&
         nodeParent.elementType == RElementTypes.R_PARAMETER_LIST &&
         (node.elementType == RElementTypes.R_PARAMETER || node.elementType == RElementTypes.R_COMMA || isCommentAtEmptyLine(node)))) {
      return childIndentAlignments[nodeParent]
    }

    if (custom.ALIGN_COMMENTS && node.elementType == RParserDefinition.END_OF_LINE_COMMENT) {
      if (nodeParent.elementType == RElementTypes.R_ARGUMENT_LIST &&
          (findPrevNonSpaceNode(node)?.elementType == RElementTypes.R_COMMA || findNextMeaningSibling(
            node)?.elementType == RElementTypes.R_RPAR)) {
        findFirstCommentAfterComma(nodeParent.firstChildNode)?.let {
          return alignmentByAnchor[it]
        }
      }
    }

    val nodeGrandParent = nodeParent.treeParent ?: return null
    if (custom.ALIGN_ASSIGNMENT_OPERATORS &&
        node.elementType == RElementTypes.R_ASSIGN_OPERATOR &&
        (nodeParent.elementType == RElementTypes.R_ASSIGNMENT_STATEMENT || nodeParent.elementType == RElementTypes.R_NAMED_ARGUMENT)) {
      if (nodeGrandParent.elementType == RElementTypes.R_ARGUMENT_LIST) {
        return assignInParametersAlignments[nodeGrandParent]
      }
      if (!isFunctionDeclarationNode(nodeParent) &&
          nodeGrandParent.elementType == RElementTypes.R_BLOCK_EXPRESSION || nodeGrandParent.elementType == RParserDefinition.FILE) {
        val anchor = findFirstAssignmentInTable(nodeParent)
        return alignmentByAnchor[anchor]
      }
    }
    return null
  }

  private fun isCommentAtEmptyLine(node: ASTNode) =
    (node.elementType == RParserDefinition.END_OF_LINE_COMMENT && findPrevNonSpaceNode(
      node)?.elementType == RElementTypes.R_NL)

  private fun findFirstCommentAfterComma(start: ASTNode?): ASTNode? {
    var current: ASTNode? = start
    while (current != null) {
      if (current.elementType == RElementTypes.R_COMMA) {
        current = current.treeNext
        while (current?.elementType == TokenType.WHITE_SPACE)
          current = current?.treeNext
        if (current?.elementType == RParserDefinition.END_OF_LINE_COMMENT) {
          return current
        }
      }
      else {
        current = current.treeNext
      }
    }
    return null
  }

  private fun findNextMeaningSibling(start: ASTNode): ASTNode? {
    var current: ASTNode? = start
    while (current != null) {
      if (!TokenSet.create(TokenType.WHITE_SPACE, RElementTypes.R_NL, RParserDefinition.END_OF_LINE_COMMENT).contains(
          current.elementType)) {
        return current
      }
      current = current.treeNext
    }
    return null
  }

  private fun findPrevNonSpaceNode(start: ASTNode): ASTNode? {
    var current: ASTNode? = start.treePrev
    while (current != null) {
      if (current.elementType != TokenType.WHITE_SPACE) {
        return current
      }
      current = current.treePrev
    }
    return null
  }

  private fun isFunctionDeclarationNode(nodeParent: ASTNode) =
    (nodeParent.psi as? RAssignmentStatement)?.isFunctionDeclaration ?: false

  private fun findFirstAssignmentInTable(start: ASTNode): ASTNode {
    var current: ASTNode? = start
    var answer: ASTNode = start
    var nlSeen = false
    while (current != null) {
      if (current.firstChildNode != null) {
        nlSeen = false
        if (current.elementType == RElementTypes.R_ASSIGNMENT_STATEMENT && !isFunctionDeclarationNode(current)) {
          answer = current
        }
        else {
          return answer
        }
      }
      if (current.elementType == RElementTypes.R_NL) {
        if (nlSeen) {
          return answer
        }
        nlSeen = true
      }
      current = current.treePrev
    }
    return answer
  }

  fun computeSpacing(parent: Block, child1: Block?, child2: Block): Spacing? = spacingBuilder.getSpacing(parent, child1, child2)

  fun computeNewChildIndent(node: ASTNode): Indent {
    return when {
      node.psi is RFile -> Indent.getNoneIndent()
      node.psi is RBlockExpression -> Indent.getNormalIndent()
      node.psi is RExpression -> Indent.getContinuationIndent()
      else -> Indent.getNoneIndent()
    }
  }

  fun computeWrap(node: ASTNode): Wrap {
    val wrapType: WrapType = if (isRightExprInOpChain(node.psi)) WrapType.ALWAYS else WrapType.NONE
    return Wrap.createWrap(wrapType, true)
  }

  fun isIncomplete(node: ASTNode): Boolean {
    if (node.elementType is ILazyParseableElementType) {
      return false
    }
    var lastChild: ASTNode? = node.lastChildNode
    while (lastChild != null &&
           lastChild.elementType !is ILazyParseableElementType &&
           (lastChild.psi is PsiWhiteSpace || lastChild.psi is PsiComment)) {
      lastChild = lastChild.treePrev
    }
    return lastChild != null && (lastChild.psi is PsiErrorElement || isIncomplete(lastChild))
  }

  fun computeBlockIndent(node: ASTNode): Indent? {
    val psi = node.psi
    return when {
      psi.parent is RFile -> Indent.getNoneIndent()
      node.elementType == RElementTypes.R_COMMA -> Indent.getContinuationIndent()
      NON_INDENT_PARTS.contains(node.elementType) -> Indent.getNoneIndent()
      psi is RParameter -> Indent.getContinuationIndent()
      psi is RBlockExpression -> Indent.getNoneIndent()
      psi is RExpression && psi.parent is RArgumentList -> Indent.getContinuationIndent()
      psi.parent is RBlockExpression -> Indent.getNormalIndent()
      psi.parent is RExpression -> Indent.getContinuationWithoutFirstIndent()
      else -> Indent.getNoneIndent()
    }
  }

  private fun isRightExprInOpChain(blockPsi: PsiElement): Boolean {
    val opExpr = blockPsi.parent as? ROperatorExpression ?: return false

    // is right expr
    if (!opExpr.isBinary || opExpr.rightExpr !== blockPsi) return false

    val chainRootExpr = getSameOpChainRoot(opExpr)

    // is actual chain to avoid wrapping of simple binary operator expressions
    val rootOperator = chainRootExpr.operator ?: return false
    val opCapture = buildOpExpCapture(rootOperator)
    // at least chain of 3
    return if (opCapture.withChild(opCapture).accepts(chainRootExpr)) chainRootExpr.text.length >= 50 else false

    // is long enough to justify wrapping
  }

  private fun buildOpExpCapture(rOperator: ROperator): PsiElementPattern.Capture<ROperatorExpression> {
    return PlatformPatterns.psiElement(ROperatorExpression::class.java).withChild(
      PlatformPatterns.psiElement(ROperator::class.java).withText(rOperator.text))
  }

  private fun getSameOpChainRoot(opExpression: ROperatorExpression): ROperatorExpression {
    var opExpr = opExpression
    while (true) {
      val opParent = opExpr.parent as? ROperatorExpression ?: return opExpr

      val exOperator = opExpr.operator ?: return opExpr
      if (opParent.operator?.text != exOperator.text) {
        return opExpr
      }
      opExpr = opParent
    }
  }
}

private fun createSpacingBuilder(settings: CodeStyleSettings): SpacingBuilder {
  val common = settings.getCommonSettings(RLanguage.INSTANCE)
  val custom = settings.getCustomSettings(RCodeStyleSettings::class.java)

  return SpacingBuilder(settings, RLanguage.INSTANCE)
    // Comments
    .before(RParserDefinition.END_OF_LINE_COMMENT).spacing(1, Int.MAX_VALUE, 0, true, common.KEEP_BLANK_LINES_IN_CODE)

    // Unary operators
    .afterInside(RElementTypes.R_TILDE_OPERATOR, RElementTypes.R_UNARY_TILDE_EXPRESSION).spaceIf(common.SPACE_AROUND_UNARY_OPERATOR)
    .afterInside(RElementTypes.R_PLUSMINUS_OPERATOR, RElementTypes.R_UNARY_PLUSMINUS_EXPRESSION).spaceIf(common.SPACE_AROUND_UNARY_OPERATOR)
    .after(RElementTypes.R_NOT_OPERATOR).spaceIf(common.SPACE_AROUND_UNARY_OPERATOR)

    // Binary operators
    .around(RElementTypes.R_ASSIGN_OPERATOR).spaceIf(common.SPACE_AROUND_ASSIGNMENT_OPERATORS)
    .aroundInside(RElementTypes.R_EQ, RElementTypes.R_PARAMETER).spaceIf(common.SPACE_AROUND_ASSIGNMENT_OPERATORS)

    .around(RElementTypes.R_TILDE_OPERATOR).spaceIf(custom.SPACE_AROUND_BINARY_TILDE_OPERATOR)
    .around(RElementTypes.R_COMPARE_OPERATOR).spaceIf(common.SPACE_AROUND_RELATIONAL_OPERATORS)
    .around(RElementTypes.R_OR_OPERATOR).spaceIf(custom.SPACE_AROUND_DISJUNCTION_OPERATORS)
    .around(RElementTypes.R_AND_OPERATOR).spaceIf(custom.SPACE_AROUND_CONJUNCTION_OPERATORS)
    .around(RElementTypes.R_PLUSMINUS_OPERATOR).spaceIf(common.SPACE_AROUND_ADDITIVE_OPERATORS)
    .around(RElementTypes.R_MULDIV_OPERATOR).spaceIf(common.SPACE_AROUND_MULTIPLICATIVE_OPERATORS)
    .around(RElementTypes.R_INFIX_OPERATOR).spaceIf(custom.SPACE_AROUND_INFIX_OPERATOR)
    .around(RElementTypes.R_COLON_OPERATOR).spaceIf(custom.SPACE_AROUND_COLON_OPERATOR)
    .around(RElementTypes.R_EXP_OPERATOR).spaceIf(custom.SPACE_AROUND_EXPONENTIATION_OPERATOR)
    .around(RElementTypes.R_LIST_SUBSET_OPERATOR).spaceIf(custom.SPACE_AROUND_SUBSET_OPERATOR)
    .around(RElementTypes.R_AT_OPERATOR).spaceIf(custom.SPACE_AROUND_AT_OPERATOR)

    // Leave this hardcoded until request (forever)
    .around(RElementTypes.R_DOUBLECOLON).spaces(0)
    .around(RElementTypes.R_TRIPLECOLON).spaces(0)

    // Parentheses group
    .beforeInside(RElementTypes.R_ARGUMENT_LIST, RElementTypes.R_CALL_EXPRESSION).spaceIf(common.SPACE_BEFORE_METHOD_CALL_PARENTHESES)
    .beforeInside(RElementTypes.R_PARAMETER_LIST, RElementTypes.R_FUNCTION_EXPRESSION).spaceIf(common.SPACE_BEFORE_METHOD_PARENTHESES)
    .between(RElementTypes.R_IF, RElementTypes.R_LPAR).spaceIf(common.SPACE_BEFORE_IF_PARENTHESES)
    .between(RElementTypes.R_WHILE, RElementTypes.R_LPAR).spaceIf(common.SPACE_BEFORE_WHILE_PARENTHESES)
    .between(RElementTypes.R_FOR, RElementTypes.R_LPAR).spaceIf(common.SPACE_BEFORE_FOR_PARENTHESES)

    // Bracket group
    .beforeInside(RElementTypes.R_BLOCK_EXPRESSION, RElementTypes.R_FUNCTION_EXPRESSION).spaceIf(common.SPACE_BEFORE_METHOD_LBRACE)
    .beforeInside(RElementTypes.R_BLOCK_EXPRESSION, RElementTypes.R_IF_STATEMENT).spaceIf(common.SPACE_BEFORE_IF_LBRACE)
    .beforeInside(RElementTypes.R_BLOCK_EXPRESSION, RElementTypes.R_WHILE_STATEMENT).spaceIf(common.SPACE_BEFORE_WHILE_LBRACE)
    .beforeInside(RElementTypes.R_BLOCK_EXPRESSION, RElementTypes.R_FOR_STATEMENT).spaceIf(common.SPACE_BEFORE_FOR_LBRACE)
    .beforeInside(RElementTypes.R_BLOCK_EXPRESSION, RElementTypes.R_REPEAT_STATEMENT).spaceIf(custom.SPACE_BEFORE_REPEAT_LBRACE)
    .before(RElementTypes.R_LBRACKET).spaceIf(custom.SPACE_BEFORE_LEFT_BRACKET)

    // Within group
    .after(RElementTypes.R_LBRACE).spaceIf(common.SPACE_WITHIN_BRACES)
    .before(RElementTypes.R_RBRACE).spaceIf(common.SPACE_WITHIN_BRACES)

    .after(RElementTypes.R_LBRACKET).spaceIf(common.SPACE_WITHIN_BRACKETS)
    .before(RElementTypes.R_RBRACKET).spaceIf(common.SPACE_WITHIN_BRACKETS)

    .after(RElementTypes.R_LPAR).spaceIf(common.SPACE_WITHIN_PARENTHESES)
    .before(RElementTypes.R_RPAR).spaceIf(common.SPACE_WITHIN_PARENTHESES)

    // Other
    .after(RElementTypes.R_COMMA).spaceIf(common.SPACE_AFTER_COMMA)
    .before(RElementTypes.R_COMMA).spaceIf(common.SPACE_BEFORE_COMMA)

    .after(RElementTypes.R_SEMI).spaces(1)

    .aroundInside(TokenSet.ANY, TokenSet.create(RParserDefinition.FILE, RElementTypes.R_BLOCK_EXPRESSION))
    .spacing(0, 0, 0, true, common.KEEP_BLANK_LINES_IN_CODE)
}
