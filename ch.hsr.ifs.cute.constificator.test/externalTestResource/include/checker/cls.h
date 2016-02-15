#ifndef __CLS_H
#define __CLS_H

struct cls
  {
  explicit cls(int number) : m_number{number} { }

  int number() const
    {
    return m_number;
    }

  void number(int const num)
    {
    m_number = num;
    }

  private:
    int m_number{};
  };

// C9
void func_ref_const(cls const &) { }
void func_ref_nonconst(cls &) { }

// C11
void func_const_ptr_const(cls const * const) { }
void func_const_ptr_nonconst(cls * const) { }
void func_nonconst_ptr_const(cls const *) { }
void func_nonconst_ptr_nonconst(cls *) { }

// C13
void func_ref_const_ptr_nonconst(cls * const &) { }
void func_ref_const_ptr_const(cls const * const &) { }

#endif __CLS_H
