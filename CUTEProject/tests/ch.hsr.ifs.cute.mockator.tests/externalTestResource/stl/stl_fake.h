#ifndef STL_FAKE_H_
#define STL_FAKE_H_

namespace std {
  template<typename _Tp>
  struct less { };

  template<typename _Tp>
  class allocator { };

  template<class _T1, class _T2>
  struct pair { };

  template <typename _Key, typename _Tp, typename _Compare = std::less<_Key>,
			   typename _Alloc = std::allocator<std::pair<const _Key, _Tp> > >
  class map { };

  template<typename _Tp, typename _Alloc = std::allocator<_Tp> >
  class vector {
  public:
	  typedef _Tp value_type;
	  void push_back(const value_type& __x);
  };

  template<typename _CharT>
  class basic_string;
  typedef basic_string<char> string;

  template<class _CharT>
  struct char_traits { };

  template<class CharT, class Traits = std::char_traits<CharT> > class basic_ostream;
  typedef basic_ostream<char> ostream;

  template<typename _CharT, typename _Traits = char_traits<_CharT>,
	   typename _Alloc = allocator<_CharT> >
    class basic_ostringstream : public basic_ostream<_CharT, _Traits> {
    };
  typedef basic_ostringstream<char> ostringstream;

  typedef unsigned long size_t;
}

#endif
