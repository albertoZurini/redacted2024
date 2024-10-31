// SPDX-License-Identifier: MIT
pragma solidity ^0.8.27;

interface IERC20 {
    function transfer(address recipient, uint256 amount) external returns (bool);
    function transferFrom(address sender, address recipient, uint256 amount) external returns (bool);
    function balanceOf(address account) external view returns (uint256);
}

contract PointOfSale {
    // Struct to store order details
    struct Order {
        address sender;
        uint256 amount;
        uint256 timestamp;
        uint256 nonce;
        bool refunded;
        bool isERC20;
        address tokenAddress;  // Only relevant if the order is paid in ERC20
    }

    // Mapping to store each order by nonce
    mapping(uint256 => Order) public orders;

    // Mapping for whitelisted ERC20 tokens
    mapping(address => bool) public tokenWhitelist;

    // Counter for the nonce
    uint256 public nonceCounter;

    // Owner of the contract (store owner)
    address public owner;

    // Event to emit when an order is received
    event OrderReceived(address indexed sender, uint256 amount, uint256 nonce, bool isERC20, address tokenAddress);
    event Withdrawal(address indexed owner, uint256 amount, address tokenAddress);
    event ItemReturned(address indexed sender, uint256 amountRefunded, uint256 nonce);
    event TokenWhitelisted(address tokenAddress);
    event TokenRemovedFromWhitelist(address tokenAddress);

    // Modifier to restrict functions to the owner
    modifier onlyOwner() {
        require(msg.sender == owner, "Not the contract owner");
        _;
    }

    // Constructor to set the contract owner
    constructor() {
        owner = msg.sender;
    }

    // Function to whitelist an ERC20 token
    function whitelistToken(address tokenAddress) external onlyOwner {
        require(tokenAddress != address(0), "Invalid token address");
        tokenWhitelist[tokenAddress] = true;
        emit TokenWhitelisted(tokenAddress);
    }

    // Function to remove an ERC20 token from the whitelist
    function removeTokenFromWhitelist(address tokenAddress) external onlyOwner {
        require(tokenWhitelist[tokenAddress], "Token not whitelisted");
        tokenWhitelist[tokenAddress] = false;
        emit TokenRemovedFromWhitelist(tokenAddress);
    }

    // Fallback function to receive Ether and record the order
    receive() external payable {
        require(msg.value > 0, "Payment must be greater than zero");

        uint256 nonce = nonceCounter++;

        // Record the order details with current timestamp
        orders[nonce] = Order({
            sender: msg.sender,
            amount: msg.value,
            timestamp: block.timestamp,
            nonce: nonce,
            refunded: false,
            isERC20: false,
            tokenAddress: address(0)
        });

        emit OrderReceived(msg.sender, msg.value, nonce, false, address(0));
    }

    // Function to record an ERC20 payment, only if token is whitelisted
    function payWithERC20(address tokenAddress, uint256 amount) external {
        require(tokenWhitelist[tokenAddress], "Token not whitelisted");
        require(amount > 0, "Payment must be greater than zero");

        require(IERC20(tokenAddress).transferFrom(msg.sender, address(this), amount), "ERC20 transfer failed");

        uint256 nonce = nonceCounter++;

        orders[nonce] = Order({
            sender: msg.sender,
            amount: amount,
            timestamp: block.timestamp,
            nonce: nonce,
            refunded: false,
            isERC20: true,
            tokenAddress: tokenAddress
        });

        emit OrderReceived(msg.sender, amount, nonce, true, tokenAddress);
    }

function batchWithdraw(uint256[] calldata orderIds) external onlyOwner {
    uint256 etherWithdrawable = 0;

    // Define an array to track token amounts and addresses for ERC20 tokens
    address[] memory tokenAddresses = new address[](orderIds.length);
    uint256[] memory tokenAmounts = new uint256[](orderIds.length);
    uint256 tokenCount = 0;

    for (uint256 i = 0; i < orderIds.length; i++) {
        uint256 id = orderIds[i];
        Order storage order = orders[id];
        
        require(!order.refunded, "Order already processed");
        require(block.timestamp >= order.timestamp + 1 weeks, "Order not yet available for withdrawal");

        if (order.isERC20) {
            bool found = false;
            for (uint256 j = 0; j < tokenCount; j++) {
                if (tokenAddresses[j] == order.tokenAddress) {
                    tokenAmounts[j] += order.amount;
                    found = true;
                    break;
                }
            }
            if (!found) {
                tokenAddresses[tokenCount] = order.tokenAddress;
                tokenAmounts[tokenCount] = order.amount;
                tokenCount++;
            }
        } else {
            etherWithdrawable += order.amount;
        }
        
        order.refunded = true;
    }

    // Withdraw Ether
    if (etherWithdrawable > 0) {
        payable(owner).transfer(etherWithdrawable);
        emit Withdrawal(owner, etherWithdrawable, address(0));
    }

    // Withdraw ERC20 tokens
    for (uint256 i = 0; i < tokenCount; i++) {
        if (tokenAmounts[i] > 0) {
            IERC20(tokenAddresses[i]).transfer(owner, tokenAmounts[i]);
            emit Withdrawal(owner, tokenAmounts[i], tokenAddresses[i]);
        }
    }
}

    // Batch return function for Ether and ERC20 orders with specified percentages
    function batchReturn(uint256[] calldata orderIds, uint256[] calldata percentages) external onlyOwner {
        require(orderIds.length == percentages.length, "Arrays must have the same length");

        for (uint256 i = 0; i < orderIds.length; i++) {
            uint256 id = orderIds[i];
            uint256 percent = percentages[i];
            require(percent > 0 && percent <= 100, "Invalid return percentage");

            Order storage order = orders[id];
            require(!order.refunded, "Order already refunded");

            uint256 refundAmount = (order.amount * percent) / 100;

            if (order.isERC20) {
                IERC20(order.tokenAddress).transfer(order.sender, refundAmount);
            } else {
                payable(order.sender).transfer(refundAmount);
            }

            order.refunded = true;
            emit ItemReturned(order.sender, refundAmount, id);
        }
    }

    // Function to check an order by nonce
    function getOrder(uint256 _nonce) external view returns (address, uint256, uint256, bool, bool, address) {
        Order memory order = orders[_nonce];
        return (order.sender, order.amount, order.timestamp, order.refunded, order.isERC20, order.tokenAddress);
    }
}
