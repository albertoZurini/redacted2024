import { context, u128, ContractPromiseBatch, PersistentMap, logging } from "near-sdk-as";

// Structure to store order details
@nearBindgen
class Order {
  sender: string;
  amount: u128;
  timestamp: u64;
  refunded: bool;

  constructor(sender: string, amount: u128, timestamp: u64) {
    this.sender = sender;
    this.amount = amount;
    this.timestamp = timestamp;
    this.refunded = false;
  }
}

// Persistent map to store orders by nonce (id)
const orders = new PersistentMap<u32, Order>("orders");

// Persistent counter for the nonce
let nonceCounter: u32 = 0;

// Owner of the contract (store owner)
const owner: string = context.sender;

// Events
function logOrderReceived(sender: string, amount: u128, nonce: u32): void {
  logging.log(`OrderReceived: sender=${sender}, amount=${amount.toString()}, nonce=${nonce}`);
}

function logWithdrawal(owner: string, amount: u128): void {
  logging.log(`Withdrawal: owner=${owner}, amount=${amount.toString()}`);
}

function logItemReturned(sender: string, refundAmount: u128, nonce: u32): void {
  logging.log(`ItemReturned: sender=${sender}, refundAmount=${refundAmount.toString()}, nonce=${nonce}`);
}

// Function to place an order by sending NEAR tokens
export function placeOrder(): void {
  assert(context.attachedDeposit > u128.Zero, "Payment must be greater than zero");

  const order = new Order(context.sender, context.attachedDeposit, context.blockTimestamp);
  orders.set(nonceCounter, order);
  logOrderReceived(context.sender, context.attachedDeposit, nonceCounter);

  nonceCounter++;
}

// Function for the owner to withdraw funds that have been in the contract for at least a week (7 * 86400000000000 yocto seconds)
export function withdrawFunds(): void {
  assert(context.sender == owner, "Only the owner can withdraw funds");

  let withdrawableAmount: u128 = u128.Zero;
  const oneWeek: u64 = 7 * 86400000000000; // 7 days in yocto seconds

  for (let i: u32 = 0; i < nonceCounter; i++) {
    const order = orders.getSome(i);
    if (!order.refunded && (context.blockTimestamp - order.timestamp) >= oneWeek) {
      withdrawableAmount = u128.add(withdrawableAmount, order.amount);
      order.refunded = true;
      orders.set(i, order); // Update order as refunded
    }
  }

  assert(withdrawableAmount > u128.Zero, "No funds available for withdrawal");

  ContractPromiseBatch.create(owner).transfer(withdrawableAmount);
  logWithdrawal(owner, withdrawableAmount);
}

// Function for the owner to process a return on an order
export function returnItem(orderId: u32, percent: u32): void {
  assert(context.sender == owner, "Only the owner can process returns");
  assert(percent > 0 && percent <= 100, "Invalid return percentage");

  const order = orders.getSome(orderId);
  assert(!order.refunded, "Order already refunded");

  const refundAmount: u128 = u128.div(u128.mul(order.amount, u128.from(percent)), u128.from(100));
  order.refunded = true;
  orders.set(orderId, order); // Update order as refunded

  ContractPromiseBatch.create(order.sender).transfer(refundAmount);
  logItemReturned(order.sender, refundAmount, orderId);
}

// Function to get an order's details
export function getOrder(orderId: u32): Order | null {
  return orders.get(orderId);
}
